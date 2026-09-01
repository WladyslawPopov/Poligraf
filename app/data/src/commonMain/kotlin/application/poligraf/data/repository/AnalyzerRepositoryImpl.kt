package application.poligraf.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import application.poligraf.database.PoligrafDatabase
import application.poligraf.domain.model.AudioFrame
import application.poligraf.domain.repository.AnalyzerRepository
import application.poligraf.engine.config.AnalyzerThresholds
import application.poligraf.engine.database.common.dbDispatcher
import application.poligraf.engine.dsp.AudioAnalyzer
import application.poligraf.engine.io.audio.AudioConstants
import application.poligraf.engine.io.audio.AudioRecorder
import application.poligraf.engine.utils.nowAsEpochMilliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class AnalyzerRepositoryImpl(
    private val recorder: AudioRecorder,
    private val db: PoligrafDatabase,
    private val scope: CoroutineScope,
) : AnalyzerRepository {

    private var analysisJob: Job? = null
    private var currentSessionId: String? = null

    private val _currentFrame = MutableStateFlow<AudioFrame?>(null)
    override val currentFrame = _currentFrame.asStateFlow()

    private val _audioFrames = MutableSharedFlow<AudioFrame>(extraBufferCapacity = 128)
    override val audioFrames = _audioFrames.asSharedFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    override val isAnalyzing = _isAnalyzing.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    override val isPaused = _isPaused.asStateFlow()

    private val _durationMillis = MutableStateFlow(0L)
    override val durationMillis = _durationMillis.asStateFlow()

    private val _calibrationProgress = MutableStateFlow(0f)
    override val calibrationProgress = _calibrationProgress.asStateFlow()

    private val _isCalibrated = MutableStateFlow(false)
    override val isCalibrated = _isCalibrated.asStateFlow()

    private val baseline =
        AudioAnalyzer.MovingBaseline(windowSize = 300) // Larger window for "Slow" baseline
    private val pitchHistory = mutableListOf<Float>()
    private val frameBatch = mutableListOf<AudioFrame>()
    private val calibrationBatch = mutableListOf<RawAtom>()

    // Global Session Profile (Reactive)
    private val _globalProfile = MutableStateFlow(AudioAnalyzer.GlobalProfile())

    // Look-ahead Queue (Verifies anomalies by looking into the "future" 600ms)
    private val lookAheadQueue = ArrayDeque<RawAtom>()

    // Atomization: 100ms windows with 50% overlap (50ms step) for "Instrument 2.1" precision
    private val windowSizeMs = 100
    private val samplesPerWindow = AudioConstants.SAMPLING_RATE / (1000 / windowSizeMs)

    private var timeBeforePause = 0L
    private var atomsProcessedInStretch = 0L

    // UI Smoothing State
    private var smoothedStress = 0f
    private var smoothedJitter = 0f
    private var smoothedPitch = 0f
    private var smoothedRms = 0f

    private fun resetInternalState() {
        baseline.reset()
        pitchHistory.clear()
        frameBatch.clear()
        calibrationBatch.clear()
        _globalProfile.value = AudioAnalyzer.GlobalProfile()
        lookAheadQueue.clear()
        _currentFrame.value = null
        _durationMillis.value = 0L
        timeBeforePause = 0L
        atomsProcessedInStretch = 0L
        _calibrationProgress.value = 0f
        _isCalibrated.value = false

        smoothedStress = 0f
        smoothedJitter = 0f
        smoothedPitch = 0f
        smoothedRms = 0f
    }

    override fun startAnalysis(title: String): String {
        if (_isAnalyzing.value && !_isPaused.value) return currentSessionId ?: ""

        if (_isAnalyzing.value && _isPaused.value) {
            resumeAnalysis()
            return currentSessionId ?: ""
        }

        resetInternalState()
        val sessionId = "session_${nowAsEpochMilliseconds()}"
        currentSessionId = sessionId

        _isAnalyzing.value = true
        _isPaused.value = false

        val startTime = nowAsEpochMilliseconds()
        scope.launch(dbDispatcher) {
            db.appDatabaseQueries.insertSession(
                id = sessionId,
                timestamp = startTime,
                title = title,
                notes = "",
                duration = 0,
                isCompleted = false,
                anomalyCount = 0
            )
        }

        startCaptureJob()
        return sessionId
    }

    override fun pauseAnalysis() {
        if (!_isAnalyzing.value || _isPaused.value) return
        val sessionId = currentSessionId ?: return
        _isPaused.value = true
        timeBeforePause = _durationMillis.value
        analysisJob?.cancel()
        recorder.stopCapture()

        // Flush remaining look-ahead atoms
        while (lookAheadQueue.isNotEmpty()) {
            finalizeFrame(lookAheadQueue.removeFirst())
        }

        flushBatch(sessionId)
    }

    override fun resumeAnalysis() {
        if (!_isAnalyzing.value || !_isPaused.value) return
        _isPaused.value = false
        atomsProcessedInStretch = 0L
        frameBatch.clear()
        startCaptureJob()
    }

    override fun resumeFromDraft(sessionId: String, lastDuration: Long) {
        if (_isAnalyzing.value) return

        currentSessionId = sessionId
        _isAnalyzing.value = true
        _isPaused.value = true
        _durationMillis.value = lastDuration
        timeBeforePause = lastDuration
        atomsProcessedInStretch = 0L
        frameBatch.clear()
    }

    private fun startCaptureJob() {
        recorder.startCapture()
        val sessionId = currentSessionId ?: return

        analysisJob = scope.launch(Dispatchers.Default) {
            val accumulatedBuffer = mutableListOf<Short>()

            // Decoupled Processing Queue (The "Neural Queue")
            val atomChannel = Channel<ShortArray>(capacity = 100)

            // 1. Profile Subscriber: Listen to calibration table changes and update global base
            launch(dbDispatcher) {
                db.appDatabaseQueries.getCalibrationData(sessionId)
                    .asFlow()
                    .mapToList(dbDispatcher)
                    .collect { rawData ->
                        val metrics = rawData.map {
                            AudioAnalyzer.AcousticMetrics(
                                it.rms.toFloat(),
                                it.pitch.toFloat(),
                                it.jitter.toFloat()
                            )
                        }
                        _globalProfile.value = AudioAnalyzer.calculateGlobalProfile(metrics)
                    }
            }

            // 2. Consumer: Background worker for "Virtual Time" analysis
            launch {
                for (atom in atomChannel) {
                    processAtom(atom)
                }
            }

            // Producer: Audio capture loop
            recorder.rawAudioFlow.collect { buffer ->
                accumulatedBuffer.addAll(buffer.toList())

                while (accumulatedBuffer.size >= samplesPerWindow) {
                    val window = accumulatedBuffer.take(samplesPerWindow).toShortArray()
                    // 50% overlap for smoothness
                    val step = samplesPerWindow / 2
                    repeat(step) { accumulatedBuffer.removeAt(0) }

                    atomChannel.send(window)
                }
            }
        }
    }

    private fun processAtom(window: ShortArray) {
        atomsProcessedInStretch++
        // Since we have 50% overlap of 100ms windows, each atom advances timeline by 50ms
        val currentDuration = timeBeforePause + (atomsProcessedInStretch * 50)
        _durationMillis.value = currentDuration

        val rms = AudioAnalyzer.calculateRms(window)
        val (pitch, confidence) = AudioAnalyzer.estimatePitchWithConfidence(
            window,
            AudioConstants.SAMPLING_RATE,
            rms
        )

        if (pitch > 50f && confidence > 0.5f) {
            pitchHistory.add(pitch)
            if (pitchHistory.size > 20) pitchHistory.removeAt(0)
        } else if (pitchHistory.isNotEmpty() && confidence < 0.3f) {
            // Drop history if signal is too noisy
            pitchHistory.removeAt(0)
        }

        val jitter = AudioAnalyzer.calculateJitter(pitchHistory)

        val atom = RawAtom(currentDuration, rms, pitch, jitter)

        // Add to Look-ahead queue
        lookAheadQueue.add(atom)

        // Store in calibration batch ONLY if it passes VAD (Voice Activity Detection)
        // This is critical for professional instruments to avoid Silence Bias.
        if (baseline.isVoice(rms, pitch)) {
            calibrationBatch.add(atom)
            if (calibrationBatch.size >= 40) { // Batch size for DB writes
                val sessionId = currentSessionId ?: return
                val toSave = calibrationBatch.toList()
                calibrationBatch.clear()
                scope.launch(dbDispatcher) { persistCalibration(sessionId, toSave) }
            }
        }

        // Wait until we have enough "future" atoms to verify the current one
        val lookAheadCount = (AnalyzerThresholds.LOOKAHEAD_WINDOW_MS / 50).toInt()
        if (lookAheadQueue.size > lookAheadCount) {
            val atomToProcess = lookAheadQueue.removeFirst()
            finalizeFrame(atomToProcess)
        }
    }

    private suspend fun persistCalibration(sessionId: String, atoms: List<RawAtom>) =
        withContext(dbDispatcher) {
            db.transaction {
                atoms.forEach { atom ->
                    db.appDatabaseQueries.insertCalibrationFrame(
                        sessionId = sessionId,
                        rms = atom.rms.toSafeDouble(),
                        pitch = atom.pitch.toSafeDouble(),
                        jitter = atom.jitter.toSafeDouble()
                    )
                }
            }
        }

    private fun finalizeFrame(atom: RawAtom) {
        val isWarmup = atom.timestamp < AnalyzerThresholds.WARMUP_DURATION_MS

        // Continuous Parallel Calibration (Dual-Track):
        // 1. Analyze against Reactive Global Profile + Look-ahead context
        val result = AudioAnalyzer.calculateHonestAnalysis(
            rms = atom.rms,
            pitch = atom.pitch,
            jitter = atom.jitter,
            baseline = baseline,
            globalProfile = _globalProfile.value,
            futureAtoms = lookAheadQueue.map {
                AudioAnalyzer.AcousticMetrics(it.rms, it.pitch, it.jitter)
            },
            isWarmup = isWarmup
        )

        // 2. Parallel baseline updates continuously
        baseline.add(
            rms = atom.rms,
            pitch = atom.pitch,
            jitter = atom.jitter,
            isAnomalyOutlier = result.isAnomaly || result.isCritical
        )

        // --- 60 FPS Visual Smoothing (EMA) ---
        // Apply smoothing to the scores before emitting to UI
        val alpha = AnalyzerThresholds.SMOOTH_LIVE
        smoothedStress = smoothedStress * (1f - alpha) + result.stressScore * alpha
        smoothedJitter = smoothedJitter * (1f - alpha) + result.jitterScore * alpha
        smoothedPitch = smoothedPitch * (1f - alpha) + result.pitchScore * alpha
        smoothedRms = smoothedRms * (1f - alpha) + result.rmsScore * alpha

        _calibrationProgress.value = if (isWarmup) {
            (atom.timestamp.toFloat() / AnalyzerThresholds.WARMUP_DURATION_MS).coerceIn(0f, 0.99f)
        } else {
            val baseProgress = baseline.getSynthesisProgress()
            if (_globalProfile.value.isReady) baseProgress else baseProgress * 0.8f
        }
        _isCalibrated.value = baseline.isSynthesized() && !isWarmup && _globalProfile.value.isReady

        val frame = AudioFrame(
            timestamp = atom.timestamp,
            rms = atom.rms,
            pitch = atom.pitch,
            jitter = atom.jitter,
            stressScore = smoothedStress,
            jitterScore = smoothedJitter,
            pitchScore = smoothedPitch,
            rmsScore = smoothedRms,
            isAnomaly = result.isAnomaly,
            isCalibrated = _isCalibrated.value,
            confidence = result.confidence,
            isCritical = result.isCritical
        )

        _currentFrame.value = frame
        _audioFrames.tryEmit(frame)

        frameBatch.add(frame)
        if (frameBatch.size >= 100) {
            val sessionId = currentSessionId ?: return
            flushBatch(sessionId)
        }
    }

    private data class RawAtom(
        val timestamp: Long,
        val rms: Float,
        val pitch: Float,
        val jitter: Float,
    )

    private fun flushBatch(sessionId: String) {
        val framesToSave = frameBatch.toList()
        frameBatch.clear()
        if (framesToSave.isNotEmpty()) {
            scope.launch(dbDispatcher) { persistFrames(sessionId, framesToSave) }
        }
    }

    private fun Float.toSafeDouble(default: Double = 0.0): Double {
        return if (this.isNaN() || this.isInfinite()) default else this.toDouble()
    }

    private suspend fun persistFrames(sessionId: String, frames: List<AudioFrame>) =
        withContext(dbDispatcher) {
            db.transaction {
                frames.forEach { frame ->
                    db.appDatabaseQueries.insertFrame(
                        sessionId = sessionId,
                        timestamp = frame.timestamp,
                        rms = frame.rms.toSafeDouble(),
                        pitch = frame.pitch.toSafeDouble(),
                        jitter = frame.jitter.toSafeDouble(),
                        stressScore = frame.stressScore.toSafeDouble(),
                        jitterScore = frame.jitterScore.toSafeDouble(),
                        pitchScore = frame.pitchScore.toSafeDouble(),
                        rmsScore = frame.rmsScore.toSafeDouble(),
                        isAnomaly = frame.isAnomaly,
                        isCalibrated = frame.isCalibrated,
                        confidence = frame.confidence.toSafeDouble(1.0),
                        isCritical = frame.isCritical
                    )
                }
                db.appDatabaseQueries.updateSessionDuration(
                    duration = _durationMillis.value,
                    id = sessionId
                )
            }
        }

    override suspend fun stopAnalysis(save: Boolean) {
        val sessionId = currentSessionId ?: return

        _isAnalyzing.value = false
        _isPaused.value = false
        analysisJob?.cancel()
        analysisJob = null
        recorder.stopCapture()
        _currentFrame.value = null

        // Flush remaining look-ahead atoms
        while (lookAheadQueue.isNotEmpty()) {
            finalizeFrame(lookAheadQueue.removeFirst())
        }

        val framesToSave = frameBatch.toList()
        frameBatch.clear()

        withContext(dbDispatcher) {
            if (framesToSave.isNotEmpty()) {
                persistFrames(sessionId, framesToSave)
            }
            // Cleanup calibration data as it's no longer needed after analysis is finalized
            db.appDatabaseQueries.deleteCalibrationData(sessionId)

            if (save) {
                val session = db.appDatabaseQueries.getSessionById(sessionId).executeAsOneOrNull()

                // Final re-calculation of anomaly count using Honest Engine logic
                val allFrames =
                    db.appDatabaseQueries.getFramesBySessionId(sessionId).executeAsList()
                        .map { entity ->
                            AudioFrame(
                                timestamp = entity.timestamp,
                                rms = entity.rms.toFloat(),
                                pitch = entity.pitch.toFloat(),
                                jitter = entity.jitter.toFloat(),
                                stressScore = entity.stressScore.toFloat(),
                                jitterScore = entity.jitterScore.toFloat(),
                                pitchScore = entity.pitchScore.toFloat(),
                                rmsScore = entity.rmsScore.toFloat(),
                                isAnomaly = entity.isAnomaly,
                                isCalibrated = entity.isCalibrated,
                                confidence = entity.confidence.toFloat(),
                                isCritical = entity.isCritical
                            )
                        }

                // We use AudioAnalyzer directly to avoid circular dependency with AnalyzerProcessor
                val metrics =
                    allFrames.map { AudioAnalyzer.AcousticMetrics(it.rms, it.pitch, it.jitter) }
                val globalProfile = AudioAnalyzer.calculateGlobalProfile(metrics)
                val dummyBaseline = AudioAnalyzer.MovingBaseline(windowSize = 200)

                var anomalyCount = 0
                val lookAheadCount = (AnalyzerThresholds.LOOKAHEAD_WINDOW_MS / 50).toInt()

                allFrames.forEachIndexed { index, frame ->
                    val futureEnd = (index + 1 + lookAheadCount).coerceAtMost(metrics.size)
                    val futureAtoms = if (index + 1 < futureEnd) metrics.subList(
                        index + 1,
                        futureEnd
                    ) else emptyList()

                    val res = AudioAnalyzer.calculateHonestAnalysis(
                        rms = frame.rms,
                        pitch = frame.pitch,
                        jitter = frame.jitter,
                        baseline = dummyBaseline,
                        globalProfile = globalProfile,
                        futureAtoms = futureAtoms,
                        isWarmup = frame.timestamp < AnalyzerThresholds.WARMUP_DURATION_MS
                    )
                    if (res.isAnomaly) anomalyCount++
                    dummyBaseline.add(frame.rms, frame.pitch, frame.jitter, res.isAnomaly)
                }

                db.appDatabaseQueries.insertSession(
                    id = sessionId,
                    timestamp = session?.timestamp ?: nowAsEpochMilliseconds(),
                    title = session?.title ?: "Session ${sessionId.takeLast(4)}",
                    notes = session?.notes ?: "",
                    duration = _durationMillis.value,
                    isCompleted = true,
                    anomalyCount = anomalyCount.toLong()
                )
            } else {
                db.appDatabaseQueries.deleteSessionById(sessionId)
            }
        }
        currentSessionId = null
    }

    override suspend fun getActiveDraft(): Pair<String, Long>? = withContext(dbDispatcher) {
        val draft = db.appDatabaseQueries.getUncompletedSession().executeAsOneOrNull()
        if (draft != null) {
            draft.id to draft.duration
        } else {
            null
        }
    }

    override fun cleanUpDrafts() {
        scope.launch(dbDispatcher) {
            val draft = db.appDatabaseQueries.getUncompletedSession().executeAsOneOrNull()
            if (draft != null) {
                db.appDatabaseQueries.deleteCalibrationData(draft.id)
            }
            db.appDatabaseQueries.deleteUncompletedSessions()
        }
    }

    override suspend fun getFramesForSession(sessionId: String): List<AudioFrame> =
        withContext(dbDispatcher) {
            db.appDatabaseQueries.getFramesBySessionId(sessionId).executeAsList().map { entity ->
                AudioFrame(
                    timestamp = entity.timestamp,
                    rms = entity.rms.toFloat(),
                    pitch = entity.pitch.toFloat(),
                    jitter = entity.jitter.toFloat(),
                    stressScore = entity.stressScore.toFloat(),
                    jitterScore = entity.jitterScore.toFloat(),
                    pitchScore = entity.pitchScore.toFloat(),
                    rmsScore = entity.rmsScore.toFloat(),
                    isAnomaly = entity.isAnomaly,
                    isCalibrated = entity.isCalibrated,
                    confidence = entity.confidence.toFloat(),
                    isCritical = entity.isCritical
                )
            }
        }
}
