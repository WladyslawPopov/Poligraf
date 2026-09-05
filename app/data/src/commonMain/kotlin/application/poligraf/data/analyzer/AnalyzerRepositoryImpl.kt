package application.poligraf.data.analyzer

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import application.poligraf.data.analyzer.dsp.AnalysisStatusResolver
import application.poligraf.data.analyzer.dsp.AnalyzerThresholds
import application.poligraf.data.analyzer.dsp.AudioAnalyzer
import application.poligraf.data.analyzer.dsp.MovingBaseline
import application.poligraf.data.analyzer.dsp.QuantumWindowAggregator
import application.poligraf.data.analyzer.model.AcousticMetrics
import application.poligraf.data.analyzer.model.GlobalProfile
import application.poligraf.data.analyzer.model.RawAtom
import application.poligraf.database.PoligrafDatabase
import application.poligraf.domain.analyzer.model.AnomalyMarker
import application.poligraf.domain.analyzer.model.AudioFrame
import application.poligraf.domain.analyzer.model.QuantumAnalysis
import application.poligraf.domain.analyzer.repository.AnalyzerRepository
import application.poligraf.domain.analyzer.types.AnalysisStatus
import application.poligraf.domain.analyzer.types.DominantMetric
import application.poligraf.domain.preferences.repository.PreferencesRepository
import application.poligraf.engine.database.common.dbDispatcher
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
    private val preferencesRepository: PreferencesRepository,
    private val scope: CoroutineScope
) : AnalyzerRepository {

    private var analysisJob: Job? = null
    private var currentSessionId: String? = null

    private val _currentFrame = MutableStateFlow<AudioFrame?>(null)
    override val currentFrame = _currentFrame.asStateFlow()

    private val _currentQuantumAnalysis = MutableStateFlow(QuantumAnalysis())
    override val currentQuantumAnalysis = _currentQuantumAnalysis.asStateFlow()

    private val _sessionMarkers = MutableStateFlow<List<AnomalyMarker>>(emptyList())
    override val sessionMarkers = _sessionMarkers.asStateFlow()

    private val _audioFrames = MutableSharedFlow<AudioFrame>(extraBufferCapacity = 128)
    override val audioFrames = _audioFrames.asSharedFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    override val isAnalyzing = _isAnalyzing.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    override val isPaused = _isPaused.asStateFlow()

    private val _durationMillis = MutableStateFlow(0L)
    override val durationMillis = _durationMillis.asStateFlow()

    private val baseline = MovingBaseline(windowSize = 300)
    private val pitchHistory = mutableListOf<Float>()
    private val frameBatch = mutableListOf<AudioFrame>()
    private val calibrationBatch = mutableListOf<RawAtom>()

    private val _globalProfile = MutableStateFlow(GlobalProfile())
    private val lookAheadQueue = ArrayDeque<RawAtom>()

    // Conversational Quantum Window Aggregator
    private val quantumSubFrames = mutableListOf<AudioFrame>()
    private var quantumWindowStart = 0L
    private var currentQuantumStatus: AnalysisStatus = AnalysisStatus.WARMUP
    private var currentPrimaryAlpha: Float = 1.0f
    private var currentSecondaryStatuses: List<AnalysisStatus> = emptyList()
    private var currentSecondaryStatusesWithScores: List<Pair<AnalysisStatus, Float>> = emptyList()

    private val windowSizeMs = 100
    private val samplesPerWindow = AudioConstants.SAMPLING_RATE / (1000 / windowSizeMs)

    private var timeBeforePause = 0L
    private var atomsProcessedInStretch = 0L

    private var smoothedStress = 0f
    private var smoothedJitter = 0f
    private var smoothedPitch = 0f
    private var smoothedRms = 0f

    private fun resetInternalState() {
        baseline.reset()
        pitchHistory.clear()
        frameBatch.clear()
        calibrationBatch.clear()
        quantumSubFrames.clear()
        quantumWindowStart = 0L
        currentQuantumStatus = AnalysisStatus.WARMUP
        currentPrimaryAlpha = 1.0f
        currentSecondaryStatuses = emptyList()
        currentSecondaryStatusesWithScores = emptyList()
        _globalProfile.value = GlobalProfile()
        lookAheadQueue.clear()
        _currentFrame.value = null
        _currentQuantumAnalysis.value = QuantumAnalysis()
        _sessionMarkers.value = emptyList()
        _durationMillis.value = 0L
        timeBeforePause = 0L
        atomsProcessedInStretch = 0L

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

        while (lookAheadQueue.isNotEmpty()) {
            finalizeFrame(lookAheadQueue.removeFirst())
        }

        flushQuantumFrame()
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
            val atomChannel = Channel<ShortArray>(capacity = 100)

            launch(dbDispatcher) {
                db.appDatabaseQueries.getCalibrationData(sessionId)
                    .asFlow()
                    .mapToList(dbDispatcher)
                    .collect { rawData ->
                        val metrics = rawData.map {
                            AcousticMetrics(
                                it.rms.toFloat(),
                                it.pitch.toFloat(),
                                it.jitter.toFloat()
                            )
                        }
                        _globalProfile.value = AudioAnalyzer.calculateGlobalProfile(metrics)
                    }
            }

            launch {
                for (atom in atomChannel) {
                    processAtom(atom)
                }
            }

            recorder.rawAudioFlow.collect { buffer ->
                accumulatedBuffer.addAll(buffer.toList())

                while (accumulatedBuffer.size >= samplesPerWindow) {
                    val window = accumulatedBuffer.take(samplesPerWindow).toShortArray()
                    val step = samplesPerWindow / 2
                    repeat(step) { accumulatedBuffer.removeAt(0) }

                    atomChannel.send(window)
                }
            }
        }
    }

    private fun processAtom(window: ShortArray) {
        atomsProcessedInStretch++
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
            pitchHistory.removeAt(0)
        }

        val jitter = AudioAnalyzer.calculateJitter(pitchHistory)

        val atom = RawAtom(currentDuration, rms, pitch, jitter)
        lookAheadQueue.add(atom)

        if (baseline.isVoice(rms, pitch)) {
            calibrationBatch.add(atom)
            if (calibrationBatch.size >= 10) {
                val sessionId = currentSessionId ?: return
                val toSave = calibrationBatch.toList()
                calibrationBatch.clear()
                scope.launch(dbDispatcher) { persistCalibration(sessionId, toSave) }
            }
        }

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
        val sensitivity = preferencesRepository.sensitivityFlow.value
        val rawFrame = AudioAnalyzer.calculateHonestAnalysis(
            timestamp = atom.timestamp,
            rms = atom.rms,
            pitch = atom.pitch,
            jitter = atom.jitter,
            baseline = baseline,
            globalProfile = _globalProfile.value,
            futureAtoms = lookAheadQueue.map {
                AcousticMetrics(it.rms, it.pitch, it.jitter)
            },
            sensitivity = sensitivity
        )

        baseline.add(
            rms = atom.rms,
            pitch = atom.pitch,
            jitter = atom.jitter,
            isAnomalyOutlier = rawFrame.isAnomaly
        )

        val isVoice = baseline.isVoice(atom.rms, atom.pitch)
        val status: AnalysisStatus

        if (!isVoice) {
            smoothedStress = 0f
            smoothedJitter = 0f
            smoothedPitch = 0f
            smoothedRms = 0f
            status = if (atom.timestamp < AnalyzerThresholds.WARMUP_DURATION_MS) {
                AnalysisStatus.WARMUP
            } else {
                AnalysisStatus.CALM
            }
        } else {
            val alpha = AnalyzerThresholds.SMOOTH_LIVE
            smoothedStress = smoothedStress * (1f - alpha) + rawFrame.stressScore * alpha
            smoothedJitter = smoothedJitter * (1f - alpha) + rawFrame.jitterScore * alpha
            smoothedPitch = smoothedPitch * (1f - alpha) + rawFrame.pitchScore * alpha
            smoothedRms = smoothedRms * (1f - alpha) + rawFrame.rmsScore * alpha

            status = AnalysisStatusResolver.resolve(
                rms = atom.rms,
                jitterScore = rawFrame.jitterScore,
                pitchScore = rawFrame.pitchScore,
                rmsScore = rawFrame.rmsScore,
                timestamp = atom.timestamp,
                sensitivity = sensitivity
            )
        }

        val atomSubFrame = AudioFrame(
            timestamp = atom.timestamp,
            stressScore = smoothedStress,
            jitterScore = smoothedJitter,
            pitchScore = smoothedPitch,
            rmsScore = smoothedRms,
            isAnomaly = rawFrame.isAnomaly,
            status = status
        )

        // Accumulate sub-frame for configurable Quantum Window status aggregation
        if (quantumSubFrames.isEmpty()) {
            quantumWindowStart = atom.timestamp
        }
        quantumSubFrames.add(atomSubFrame)

        val displaySubFrame = AudioFrame(
            timestamp = atom.timestamp,
            stressScore = smoothedStress,
            jitterScore = smoothedJitter,
            pitchScore = smoothedPitch,
            rmsScore = smoothedRms,
            isAnomaly = rawFrame.isAnomaly,
            status = currentQuantumStatus,
            primaryAlpha = currentPrimaryAlpha,
            secondaryStatuses = currentSecondaryStatuses,
            secondaryStatusesWithScores = currentSecondaryStatusesWithScores
        )

        // Real-time emission at 20 FPS (every 50ms) with stable quantum window status
        _currentFrame.value = displaySubFrame
        _audioFrames.tryEmit(displaySubFrame)

        frameBatch.add(displaySubFrame)
        if (frameBatch.size >= 100) { // Save batch every 100 frames = 5 seconds at 20 FPS
            val sessionId = currentSessionId ?: return
            flushBatch(sessionId)
        }

        val quantumWindowDurationMs = preferencesRepository.quantumWindowFlow.value.millis
        if ((atom.timestamp - quantumWindowStart) >= quantumWindowDurationMs) {
            flushQuantumFrame()
        }
    }

    private fun flushQuantumFrame() {
        if (quantumSubFrames.isEmpty()) return

        val sensitivity = preferencesRepository.sensitivityFlow.value
        val quantumWindowMs = preferencesRepository.quantumWindowFlow.value.millis
        val quantumAnalysis = QuantumWindowAggregator.aggregateWindow(quantumSubFrames, sensitivity)

        currentQuantumStatus = quantumAnalysis.primaryStatus
        currentPrimaryAlpha = quantumAnalysis.primaryAlpha
        currentSecondaryStatuses = quantumAnalysis.secondaryStatuses
        currentSecondaryStatusesWithScores = quantumAnalysis.secondaryStatusesWithScores
        _currentQuantumAnalysis.value = quantumAnalysis

        val newWindowMarkers = QuantumWindowAggregator.extractWindowMarkers(
            quantumSubFrames,
            quantumWindowMs,
            sensitivity
        )

        if (newWindowMarkers.isNotEmpty()) {
            val currentMarkers = _sessionMarkers.value.toMutableList()
            var addedAny = false
            for (marker in newWindowMarkers) {
                val index = currentMarkers.indexOfFirst { it.id == marker.id }
                if (index >= 0) {
                    currentMarkers[index] = marker
                    addedAny = true
                } else {
                    currentMarkers.add(marker)
                    addedAny = true
                }
            }
            if (addedAny) {
                _sessionMarkers.value = currentMarkers.toList()
                val sessionId = currentSessionId
                if (sessionId != null) {
                    val toPersist = newWindowMarkers.toList()
                    scope.launch(dbDispatcher) {
                        persistMarkers(sessionId, toPersist)
                    }
                }
            }
        }

        quantumSubFrames.clear()
    }

    private suspend fun persistMarkers(sessionId: String, markers: List<AnomalyMarker>) =
        withContext(dbDispatcher) {
            db.transaction {
                markers.forEach { marker ->
                    db.appDatabaseQueries.insertMarker(
                        id = marker.id,
                        sessionId = sessionId,
                        timestamp = marker.timestampMillis,
                        status = marker.status.name,
                        dominantMetric = marker.dominantMetric.name,
                        isFullAnomaly = marker.isFullAnomaly,
                        alpha = marker.alpha.toDouble()
                    )
                }
            }
        }

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
                        stressScore = frame.stressScore.toSafeDouble(),
                        jitterScore = frame.jitterScore.toSafeDouble(),
                        pitchScore = frame.pitchScore.toSafeDouble(),
                        rmsScore = frame.rmsScore.toSafeDouble(),
                        isAnomaly = frame.isAnomaly
                    )
                }
                db.appDatabaseQueries.updateSessionDuration(
                    duration = _durationMillis.value,
                    id = sessionId
                )
            }
        }

    override suspend fun stopAnalysis(save: Boolean, anomalyCount: Long) {
        val sessionId = currentSessionId ?: return

        _isAnalyzing.value = false
        _isPaused.value = false
        analysisJob?.cancel()
        analysisJob = null
        recorder.stopCapture()
        _currentFrame.value = null

        while (lookAheadQueue.isNotEmpty()) {
            finalizeFrame(lookAheadQueue.removeFirst())
        }

        flushQuantumFrame()

        val framesToSave = frameBatch.toList()
        frameBatch.clear()

        withContext(dbDispatcher) {
            if (framesToSave.isNotEmpty()) {
                persistFrames(sessionId, framesToSave)
            }
            db.appDatabaseQueries.deleteCalibrationData(sessionId)

            if (save) {
                val session = db.appDatabaseQueries.getSessionById(sessionId).executeAsOneOrNull()

                db.appDatabaseQueries.insertSession(
                    id = sessionId,
                    timestamp = session?.timestamp ?: nowAsEpochMilliseconds(),
                    title = session?.title ?: "Session ${sessionId.takeLast(4)}",
                    notes = session?.notes ?: "",
                    duration = _durationMillis.value,
                    isCompleted = true,
                    anomalyCount = anomalyCount
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
            val sensitivity = preferencesRepository.sensitivityFlow.value
            db.appDatabaseQueries.getFramesBySessionId(sessionId).executeAsList().map { entity ->
                val rmsScore = entity.rmsScore.toFloat()
                val jitterScore = entity.jitterScore.toFloat()
                val pitchScore = entity.pitchScore.toFloat()
                val stressScore = entity.stressScore.toFloat()

                val status = AnalysisStatusResolver.resolve(
                    rms = rmsScore,
                    jitterScore = jitterScore,
                    pitchScore = pitchScore,
                    rmsScore = rmsScore,
                    timestamp = entity.timestamp,
                    sensitivity = sensitivity
                )
                AudioFrame(
                    timestamp = entity.timestamp,
                    stressScore = stressScore,
                    jitterScore = jitterScore,
                    pitchScore = pitchScore,
                    rmsScore = rmsScore,
                    isAnomaly = entity.isAnomaly,
                    status = status
                )
            }
        }

    override suspend fun getMarkersForSession(sessionId: String): List<AnomalyMarker> =
        withContext(dbDispatcher) {
            val dbMarkers = db.appDatabaseQueries.getMarkersBySessionId(sessionId).executeAsList()
            if (dbMarkers.isNotEmpty()) {
                dbMarkers.mapNotNull { entity ->
                    try {
                        AnomalyMarker(
                            id = entity.id,
                            timestampMillis = entity.timestamp,
                            status = AnalysisStatus.valueOf(entity.status),
                            dominantMetric = DominantMetric.valueOf(entity.dominantMetric),
                            isFullAnomaly = entity.isFullAnomaly,
                            alpha = entity.alpha.toFloat()
                        )
                    } catch (_: Exception) {
                        null
                    }
                }
            } else {
                val frames = getFramesForSession(sessionId)
                val sensitivity = preferencesRepository.sensitivityFlow.value
                val quantumWindowMs = preferencesRepository.quantumWindowFlow.value.millis
                val extracted = QuantumWindowAggregator.extractWindowMarkers(frames, quantumWindowMs, sensitivity)
                if (extracted.isNotEmpty()) {
                    persistMarkers(sessionId, extracted)
                }
                extracted
            }
        }
}
