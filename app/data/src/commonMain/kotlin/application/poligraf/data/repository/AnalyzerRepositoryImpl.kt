package application.poligraf.data.repository

import application.poligraf.database.PoligrafDatabase
import application.poligraf.domain.model.AudioFrame
import application.poligraf.domain.repository.AnalyzerRepository
import application.poligraf.engine.database.common.dbDispatcher
import application.poligraf.engine.dsp.AudioAnalyzer
import application.poligraf.engine.io.audio.AudioConstants
import application.poligraf.engine.io.audio.AudioRecorder
import application.poligraf.engine.utils.nowAsEpochMilliseconds
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

internal class AnalyzerRepositoryImpl(
    private val recorder: AudioRecorder,
    private val db: PoligrafDatabase,
    private val scope: CoroutineScope
) : AnalyzerRepository {
    
    private var analysisJob: Job? = null
    private var currentSessionId: String? = null
    
    private val _currentFrame = MutableStateFlow<AudioFrame?>(null)
    override val currentFrame = _currentFrame.asStateFlow()

    private val _audioFrames = MutableSharedFlow<AudioFrame>(extraBufferCapacity = 128)
    override val audioFrames = _audioFrames.asSharedFlow()

    private val _isAnomalous = MutableStateFlow(false)
    override val isAnomalous = _isAnomalous.asStateFlow()

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

    private val baseline = AudioAnalyzer.MovingBaseline(windowSize = 300) // Larger window for "Slow" baseline
    private val pitchHistory = mutableListOf<Float>()
    private val frameBatch = mutableListOf<AudioFrame>()
    
    // Atomization: 50ms windows with 75% overlap for "Instrument 2.1" precision
    private val windowSizeMs = 100 
    private val samplesPerWindow = AudioConstants.SAMPLING_RATE / (1000 / windowSizeMs)
    
    private var timeBeforePause = 0L
    private var atomsProcessedInStretch = 0L

    private fun resetInternalState() {
        baseline.reset()
        pitchHistory.clear()
        frameBatch.clear()
        _currentFrame.value = null
        _durationMillis.value = 0L
        timeBeforePause = 0L
        atomsProcessedInStretch = 0L
        _calibrationProgress.value = 0f
        _isCalibrated.value = false
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
                isCompleted = false
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
        analysisJob = scope.launch(Dispatchers.Default) {
            val accumulatedBuffer = mutableListOf<Short>()
            
            // Decoupled Processing Queue (The "Neural Queue")
            val atomChannel = Channel<ShortArray>(capacity = 100)
            
            // Consumer: Background worker for "Virtual Time" analysis
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
        val pitch = AudioAnalyzer.estimatePitch(window, AudioConstants.SAMPLING_RATE, rms)
        
        if (pitch > 50f) {
            pitchHistory.add(pitch)
            if (pitchHistory.size > 20) pitchHistory.removeAt(0)
        } else if (pitchHistory.isNotEmpty()) {
            pitchHistory.removeAt(0)
        }
        
        val jitter = AudioAnalyzer.calculateJitter(pitchHistory)

        // Continuous Parallel Calibration (Dual-Track):
        // 1. First analyze against current baseline
        val result = AudioAnalyzer.calculateAdvancedAnalysis(
            rms = rms,
            pitch = pitch,
            jitter = jitter,
            baseline = baseline
        )

        // 2. Parallel baseline updates continuously; stress outbursts/anomalies are rejected to avoid corrupting calibration
        baseline.add(
            rms = rms,
            pitch = pitch,
            jitter = jitter,
            isAnomalyOutlier = result.isAnomaly || result.isCritical
        )
        _calibrationProgress.value = baseline.getSynthesisProgress()
        _isCalibrated.value = baseline.isSynthesized()
        
        val frame = AudioFrame(
            timestamp = currentDuration,
            rms = rms,
            pitch = pitch,
            jitter = jitter,
            stressScore = result.stressScore,
            jitterScore = result.jitterScore,
            pitchScore = result.pitchScore,
            rmsScore = result.rmsScore,
            isAnomaly = result.isAnomaly,
            isCalibrated = baseline.isSynthesized(),
            confidence = result.confidence,
            isCritical = result.isCritical
        )
        
        _currentFrame.value = frame
        _isAnomalous.value = result.isVisualAnomaly // Glow reacts to 2.0 sigma
        _audioFrames.tryEmit(frame)
        
        frameBatch.add(frame)
        if (frameBatch.size >= 100) {
            val sessionId = currentSessionId ?: return
            flushBatch(sessionId)
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

    private suspend fun persistFrames(sessionId: String, frames: List<AudioFrame>) = withContext(dbDispatcher) {
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
        
        val framesToSave = frameBatch.toList()
        frameBatch.clear()
        
        withContext(dbDispatcher) {
            if (framesToSave.isNotEmpty()) {
                persistFrames(sessionId, framesToSave)
            }
            if (save) {
                val session = db.appDatabaseQueries.getSessionById(sessionId).executeAsOneOrNull()
                db.appDatabaseQueries.insertSession(
                    id = sessionId,
                    timestamp = session?.timestamp ?: nowAsEpochMilliseconds(),
                    title = session?.title ?: "Session ${sessionId.takeLast(4)}",
                    notes = session?.notes ?: "",
                    duration = _durationMillis.value,
                    isCompleted = true
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
            db.appDatabaseQueries.deleteUncompletedSessions()
        }
    }

    override suspend fun getFramesForSession(sessionId: String): List<AudioFrame> = withContext(dbDispatcher) {
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
