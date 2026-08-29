package application.poligraf.presentation.analyzer

import androidx.compose.runtime.Stable
import application.poligraf.domain.model.AnalyzerSkin
import application.poligraf.domain.model.AudioFrame
import application.poligraf.domain.model.MarkerShape
import application.poligraf.domain.repository.AnalyzerRepository
import application.poligraf.domain.repository.HistoryRepository
import application.poligraf.domain.repository.PreferencesRepository
import application.poligraf.engine.dsp.AudioAnalyzer
import application.poligraf.engine.io.audio.AudioConstants
import application.poligraf.presentation.analyzer.data.AnalyzerState
import application.poligraf.presentation.base.BaseViewModel
import application.poligraf.ui.foundation.actions.RecordingAction
import application.poligraf.ui.foundation.actions.WidgetAction
import application.poligraf.ui.foundation.models.AnalyzerMarker
import application.poligraf.ui.foundation.models.AppToolbar
import application.poligraf.ui.foundation.models.ToolbarAction
import application.poligraf.ui.theme.IAppStrings
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.IconToken
import application.poligraf.ui.theme.tokens.StringToken
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@Stable
class AnalyzerViewModel(
    private val repository: AnalyzerRepository,
    private val historyRepository: HistoryRepository,
    val preferencesRepository: PreferencesRepository,
    private val appStrings: IAppStrings
) : BaseViewModel() {

    private val _state = MutableStateFlow(
        AnalyzerState(
            toolbar = AppToolbar(
                titleToken = StringToken.RECORDER_TITLE,
                backgroundColor = ColorToken.SURFACE_BACKGROUND,
                contentColor = ColorToken.TEXT_PRIMARY,
                trailingActions = listOf(
                    ToolbarAction(
                        icon = IconToken.DELETE,
                        action = RecordingAction.Delete,
                        tint = ColorToken.STATE_ERROR
                    ),
                    ToolbarAction(
                        icon = IconToken.CHECK,
                        action = RecordingAction.Save,
                        tint = ColorToken.STATE_SUCCESS
                    )
                )
            )
        )
    )
    val state = _state.asStateFlow()

    private val _navigateToDetail = MutableSharedFlow<String>()
    val navigateToDetail = _navigateToDetail.asSharedFlow()

    private val timelineMarkers = mutableListOf<AnalyzerMarker>()
    private val frameHistory = mutableListOf<AudioFrame>()
    private var currentSessionId: String? = null

    private var currentMarkerShape: MarkerShape = MarkerShape.CIRCLE

    private val baseline = AudioAnalyzer.MovingBaseline()

    // Smoothing buffers for UI
    private var smoothedJitter = 0f
    private var smoothedPitch = 0f
    private var smoothedRms = 0f

    // Sticky interpretation logic
    private var lastInterpretation: StringToken? = null
    private var interpretationTimestamp = 0L

    init {
        // Observe preferences
        scope.launch {
            preferencesRepository.markerShape.collect { shape ->
                currentMarkerShape = shape
                // Update existing markers shape
                val updated = timelineMarkers.map { it.copy(shape = shape) }
                timelineMarkers.clear()
                timelineMarkers.addAll(updated)
                _state.update { it.copy(timelineMarkers = timelineMarkers.toList()) }
            }
        }

        scope.launch {
            preferencesRepository.defaultSkin.collect { skin ->
                _state.update { it.copy(currentSkin = skin) }
            }
        }

        // Continuous history recording from SharedFlow (No conflation)
        scope.launch {
            repository.audioFrames.collect { frame ->
                val lastTimestamp = frameHistory.lastOrNull()?.timestamp ?: -1L
                if (frame.timestamp > lastTimestamp) {
                    frameHistory.add(frame)
                    processAnomalyMarker(frame, frame.timestamp)
                    baseline.add(frame.rms, frame.pitch)
                }
            }
        }

        // Observe repository state for UI updates
        scope.launch {
            combine(
                repository.currentFrame,
                repository.durationMillis,
                repository.isRecording,
                repository.isPaused,
                repository.isAnomalous
            ) { frame, duration, recording, paused, anomalous ->

                _state.update {
                    it.copy(
                        currentFrame = frame,
                        durationText = formatDuration(duration),
                        currentDurationMillis = duration,
                        isRecording = recording,
                        isPaused = paused,
                        isAnomalous = anomalous,
                        isCalibrated = baseline.isCalibrated(),
                        timelineMarkers = timelineMarkers.toList()
                    )
                }
            }.collect()
        }

        // Separate observer for Seek & Display Logic
        scope.launch {
            _state.map { Triple(it.seekPositionMillis, it.isPaused, it.currentFrame) }
                .distinctUntilChanged()
                .collect {
                    updateDisplayState()
                }
        }
    }

    private var smoothedStress = 0f

    private fun updateDisplayState() {
        val currentState = _state.value
        val seekPos = currentState.seekPositionMillis
        val isPaused = currentState.isPaused

        val activeFrame = if (isPaused && seekPos != null) {
            findClosestFrame(seekPos)
        } else {
            currentState.currentFrame
        }

        // Reset stickiness when changing seek position significantly or switching modes
        if (isPaused) {
            lastInterpretation = null
            interpretationTimestamp = 0
        }

        val rawJitter = activeFrame?.jitter ?: 0f
        val rawPitch = activeFrame?.pitch ?: 0f
        val rawRms = activeFrame?.rms ?: 0f
        val rawStress = activeFrame?.stressScore ?: 0f

        // Normalize raw values to 0..1 range for UI
        val targetJitter = (rawJitter / AudioConstants.MAX_EXPECTED_JITTER).coerceIn(0f, 1f)
        val targetPitch = if (rawPitch > 50f) {
            ((rawPitch - AudioConstants.MIN_EXPECTED_PITCH) / (AudioConstants.MAX_EXPECTED_PITCH - AudioConstants.MIN_EXPECTED_PITCH))
                .coerceIn(0f, 1f)
        } else 0f
        val targetRms = (rawRms * AudioConstants.RMS_VISUAL_AMPLIFIER).coerceIn(0f, 1f)

        // Apply Exponential Moving Average (EMA) smoothing for visualization stability
        val alpha = if (isPaused) 0.35f else 0.15f
        smoothedJitter = (targetJitter * alpha) + (smoothedJitter * (1f - alpha))
        smoothedPitch = (targetPitch * alpha) + (smoothedPitch * (1f - alpha))
        smoothedRms = (targetRms * alpha) + (smoothedRms * (1f - alpha))

        // Smooth the anomaly / stress detection to avoid flickering glow
        val stressAlpha = if (isPaused) 0.40f else 0.25f
        smoothedStress = (rawStress * stressAlpha) + (smoothedStress * (1f - stressAlpha))

        // Process interpretation with stickiness (Only in Live mode)
        val currentInterpretation =
            determineInterpretation(smoothedJitter, smoothedPitch, smoothedRms)
        val now = application.poligraf.engine.utils.nowAsEpochMilliseconds()

        val finalInterpretation = if (currentInterpretation != null) {
            lastInterpretation = currentInterpretation
            interpretationTimestamp = now
            currentInterpretation
        } else if (!isPaused && lastInterpretation != null && (now - interpretationTimestamp) < AudioConstants.INTERPRETATION_STICKY_MS) {
            // Keep the previous one visible for the configured duration
            lastInterpretation
        } else {
            lastInterpretation = null
            null
        }

        val isAnomalous =
            if (isPaused) (activeFrame?.isAnomaly ?: false) else ((activeFrame?.isAnomaly ?: false) || smoothedStress > 0.50f)

        _state.update {
            it.copy(
                displayFrame = activeFrame,
                jitterLevel = smoothedJitter,
                pitchLevel = smoothedPitch,
                rmsLevel = smoothedRms,
                activeInterpretation = finalInterpretation,
                isDisplayAnomalous = isAnomalous,
                isCalibrated = activeFrame?.isCalibrated
                    ?: (if (isPaused) true else it.isCalibrated)
            )
        }
    }

    private fun determineInterpretation(jitter: Float, pitch: Float, rms: Float): StringToken? {
        val isJitter = jitter > 0.4f
        val isPitch = pitch > 0.4f
        val isRms = rms > 0.4f

        return when {
            isJitter && isPitch && isRms -> StringToken.INTERPRETATION_DISORGANIZATION
            isJitter && isPitch && !isRms -> StringToken.INTERPRETATION_PANIC
            isJitter && isRms && !isPitch -> StringToken.INTERPRETATION_AGGRESSION
            isPitch && isRms && !isJitter -> StringToken.INTERPRETATION_CONFRONTATION
            else -> null
        }
    }

    private fun findClosestFrame(seekPos: Long): AudioFrame? {
        if (frameHistory.isEmpty()) return null

        // Binary search for efficiency
        var low = 0
        var high = frameHistory.size - 1

        while (low <= high) {
            val mid = (low + high) / 2
            val midVal = frameHistory[mid].timestamp

            when {
                midVal < seekPos -> low = mid + 1
                midVal > seekPos -> high = mid - 1
                else -> return frameHistory[mid]
            }
        }

        val index = low.coerceIn(0, frameHistory.size - 1)
        return frameHistory[index]
    }

    private fun processAnomalyMarker(frame: AudioFrame, duration: Long) {
        if (frame.isAnomaly) {
            val dominantColor = when {
                frame.jitter > 30f -> ColorToken.CHART_JITTER
                frame.pitch > 200f -> ColorToken.CHART_PITCH
                else -> ColorToken.CHART_RMS
            }

            val marker = AnalyzerMarker(
                id = "m_$duration",
                timestampMillis = duration,
                timestampText = formatDuration(duration),
                colorToken = dominantColor,
                isAnomaly = true,
                shape = currentMarkerShape
            )
            if (timelineMarkers.none { it.timestampText == marker.timestampText }) {
                timelineMarkers.add(marker)
            }
        }
    }

    fun onAppear() {
        scope.launch {
            val isRecording = repository.isRecording.value
            val draft = repository.getActiveDraft()

            if (isRecording) {
                if (draft != null) {
                    currentSessionId = draft.first
                    if (frameHistory.isEmpty()) {
                        loadSessionHistory(draft.first)
                    }
                }
                return@launch
            }

            if (draft != null) {
                currentSessionId = draft.first
                loadSessionHistory(draft.first)
                repository.resumeFromDraft(draft.first, draft.second)
            } else {
                onStart()
            }
        }
    }

    private suspend fun loadSessionHistory(sessionId: String) {
        val frames = repository.getFramesForSession(sessionId)
        frameHistory.clear()
        frameHistory.addAll(frames)

        timelineMarkers.clear()
        frames.forEach { processAnomalyMarker(it, it.timestamp) }
    }

    fun onSeek(positionMillis: Long?) {
        _state.update { it.copy(seekPositionMillis = positionMillis) }
    }

    fun onStart() {
        scope.launch {
            val count = historyRepository.getSessionCount()
            val defaultTitle = "Session #${count + 1}"
            frameHistory.clear()
            timelineMarkers.clear()
            
            currentSessionId = repository.startAnalysis(defaultTitle)
        }
    }

    fun onPauseResume() {
        if (repository.isPaused.value) {
            // When resuming, clear seek position to jump back to live
            _state.update { it.copy(seekPositionMillis = null) }
            repository.resumeAnalysis()
        } else {
            repository.pauseAnalysis()
        }
    }

    fun onStop(save: Boolean) {
        if (_state.value.isProcessing) return
        
        val sessionId = currentSessionId
        if (save && sessionId == null) return

        _state.update { it.copy(isProcessing = true) }

        repository.stopAnalysis(save)
        
        if (save && sessionId != null) {
            scope.launch {
                _navigateToDetail.emit(sessionId)
            }
        }
    }

    fun onAction(action: WidgetAction) {
        when (action) {
            is RecordingAction.Save -> onStop(true)
            is RecordingAction.Delete -> onStop(false)
            else -> {}
        }
    }

    fun onBack() {
        // Here we could handle auto-save or something if needed
    }

    fun onSkinChange(skin: AnalyzerSkin) {
        preferencesRepository.setDefaultSkin(skin)
        _state.update { it.copy(currentSkin = skin) }
    }

    private fun formatDuration(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    }
}
