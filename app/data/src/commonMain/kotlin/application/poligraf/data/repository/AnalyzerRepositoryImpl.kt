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

    private val _isAnomalous = MutableStateFlow(false)
    override val isAnomalous = _isAnomalous.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    override val isRecording = _isRecording.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    override val isPaused = _isPaused.asStateFlow()

    private val _durationMillis = MutableStateFlow(0L)
    override val durationMillis = _durationMillis.asStateFlow()

    private val baseline = AudioAnalyzer.MovingBaseline()
    private val pitchHistory = mutableListOf<Float>()
    private val frameBatch = mutableListOf<AudioFrame>()
    
    private val samplesPer100ms = AudioConstants.SAMPLING_RATE / 10
    private var sessionStartTime = 0L
    private var timeBeforePause = 0L

    private fun resetInternalState() {
        baseline.reset()
        pitchHistory.clear()
        frameBatch.clear()
        _currentFrame.value = null
        _durationMillis.value = 0L
        timeBeforePause = 0L
    }

    override fun startAnalysis() {
        if (_isRecording.value && !_isPaused.value) return
        
        // If already recording but paused, just resume
        if (_isRecording.value && _isPaused.value) {
            resumeAnalysis()
            return
        }

        resetInternalState()
        val sessionId = "session_${nowAsEpochMilliseconds()}"
        currentSessionId = sessionId
        
        _isRecording.value = true
        _isPaused.value = false
        sessionStartTime = nowAsEpochMilliseconds()

        // Create draft session in DB
        scope.launch(dbDispatcher) {
            db.appDatabaseQueries.insertSession(
                id = sessionId,
                timestamp = sessionStartTime,
                title = "New Session",
                notes = "",
                duration = 0,
                isCompleted = false
            )
        }
        
        startCaptureJob()
    }

    override fun pauseAnalysis() {
        if (!_isRecording.value || _isPaused.value) return
        val sessionId = currentSessionId ?: return
        _isPaused.value = true
        timeBeforePause = _durationMillis.value
        analysisJob?.cancel()
        recorder.stopCapture()
        
        val framesToSave = frameBatch.toList()
        frameBatch.clear()
        if (framesToSave.isNotEmpty()) {
            scope.launch(dbDispatcher) { persistFrames(sessionId, framesToSave) }
        }
    }

    override fun resumeAnalysis() {
        if (!_isRecording.value || !_isPaused.value) return
        _isPaused.value = false
        sessionStartTime = nowAsEpochMilliseconds()
        frameBatch.clear()
        startCaptureJob()
    }

    override fun resumeFromDraft(sessionId: String, lastDuration: Long) {
        if (_isRecording.value) return
        
        currentSessionId = sessionId
        _isRecording.value = true
        _isPaused.value = true 
        _durationMillis.value = lastDuration
        timeBeforePause = lastDuration
        sessionStartTime = nowAsEpochMilliseconds()
        frameBatch.clear()
    }

    private fun startCaptureJob() {
        recorder.startCapture()
        analysisJob = scope.launch(Dispatchers.Default) {
            val accumulatedBuffer = mutableListOf<Short>()
            
            recorder.rawAudioFlow.collect { buffer ->
                accumulatedBuffer.addAll(buffer.toList())
                
                while (accumulatedBuffer.size >= samplesPer100ms) {
                    val window = accumulatedBuffer.take(samplesPer100ms).toShortArray()
                    repeat(samplesPer100ms) { accumulatedBuffer.removeAt(0) }
                    
                    val currentDuration = timeBeforePause + (nowAsEpochMilliseconds() - sessionStartTime)
                    _durationMillis.value = currentDuration
                    
                    processWindow(window, currentDuration)
                }
            }
        }
    }

    private fun processWindow(window: ShortArray, currentDuration: Long) {
        val rms = AudioAnalyzer.calculateRms(window)
        val pitch = AudioAnalyzer.estimatePitch(window, AudioConstants.SAMPLING_RATE, rms)
        
        if (pitch > 50f) {
            pitchHistory.add(pitch)
            if (pitchHistory.size > 15) pitchHistory.removeAt(0)
        } else {
            if (pitchHistory.isNotEmpty()) {
                pitchHistory.removeAt(0)
            }
        }
        
        val jitter = AudioAnalyzer.calculateJitter(pitchHistory)
        baseline.add(rms, pitch)
        
        val stressScore = AudioAnalyzer.calculateStressScore(
            rms = rms,
            pitch = pitch,
            jitter = jitter,
            baselineRms = baseline.getAvgRms(),
            baselinePitch = baseline.getAvgPitch()
        )
        
        val frame = AudioFrame(
            timestamp = currentDuration,
            rms = rms,
            pitch = pitch,
            jitter = jitter,
            stressScore = stressScore,
            isAnomaly = stressScore > 0.65f,
            isCalibrated = baseline.isCalibrated()
        )
        
        _currentFrame.value = frame
        _isAnomalous.value = frame.isAnomaly
        
        frameBatch.add(frame)
        if (frameBatch.size >= 50) {
            val framesToSave = frameBatch.toList()
            frameBatch.clear()
            val sessionId = currentSessionId
            if (sessionId != null) {
                scope.launch(dbDispatcher) { persistFrames(sessionId, framesToSave) }
            }
        }
    }

    private suspend fun persistFrames(sessionId: String, frames: List<AudioFrame>) = withContext(dbDispatcher) {
        db.transaction {
            frames.forEach { frame ->
                db.appDatabaseQueries.insertFrame(
                    sessionId = sessionId,
                    timestamp = frame.timestamp,
                    rms = frame.rms.toDouble(),
                    pitch = frame.pitch.toDouble(),
                    jitter = frame.jitter.toDouble(),
                    stressScore = frame.stressScore.toDouble(),
                    isAnomaly = frame.isAnomaly,
                    isCalibrated = frame.isCalibrated
                )
            }
            db.appDatabaseQueries.updateSessionDuration(
                duration = _durationMillis.value,
                id = sessionId
            )
        }
    }

    override fun stopAnalysis(save: Boolean) {
        val sessionId = currentSessionId ?: return
        
        _isRecording.value = false
        _isPaused.value = false
        analysisJob?.cancel()
        analysisJob = null
        recorder.stopCapture()
        _currentFrame.value = null
        
        val finalFrames = frameBatch.toList()
        frameBatch.clear()

        scope.launch(dbDispatcher) {
            if (finalFrames.isNotEmpty()) {
                persistFrames(sessionId, finalFrames)
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
                isAnomaly = entity.isAnomaly,
                isCalibrated = entity.isCalibrated
            )
        }
    }
}
