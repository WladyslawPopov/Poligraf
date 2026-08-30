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
import application.poligraf.presentation.analyzer.logic.AnalyzerProcessor
import application.poligraf.presentation.base.BaseViewModel
import application.poligraf.ui.foundation.actions.AnalyzingAction
import application.poligraf.ui.foundation.models.AnalyzerMarker
import application.poligraf.ui.foundation.models.AppToolbar
import application.poligraf.ui.foundation.models.ToolbarAction
import application.poligraf.ui.foundation.state.AnalyzerState
import application.poligraf.ui.foundation.models.SessionNoteUiModel
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.IconToken
import application.poligraf.ui.theme.tokens.StringToken
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@Stable
class AnalyzerViewModel(
    private val repository: AnalyzerRepository,
    private val historyRepository: HistoryRepository,
    val preferencesRepository: PreferencesRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow(
        AnalyzerState(
            toolbar = AppToolbar(
                titleToken = StringToken.ANALYZER_TITLE,
                backgroundColor = ColorToken.SURFACE_BACKGROUND,
                contentColor = ColorToken.TEXT_PRIMARY,
                trailingActions = listOf(
                    ToolbarAction(
                        icon = IconToken.DELETE,
                        action = AnalyzingAction.Delete,
                        tint = ColorToken.STATE_ERROR
                    ),
                    ToolbarAction(
                        icon = IconToken.CHECK,
                        action = AnalyzingAction.Save,
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
    private val _currentSessionIdFlow = MutableStateFlow<String?>(null)
    private var currentSessionId: String? = null
        set(value) {
            field = value
            _currentSessionIdFlow.value = value
        }

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

                    val lastMarkerTime = timelineMarkers.lastOrNull()?.timestampMillis ?: 0L
                    AnalyzerProcessor.createAnomalyMarker(frame, currentMarkerShape, lastMarkerTime)
                        ?.let {
                            timelineMarkers.add(it)
                        }
                    baseline.add(frame.rms, frame.pitch, frame.jitter)
                }
            }
        }

        // Observe repository state for UI updates
        scope.launch {
            combine(
                repository.currentFrame,
                repository.durationMillis,
                repository.isAnalyzing,
                repository.isPaused,
                repository.isAnomalous,
                repository.calibrationProgress,
                repository.isCalibrated
            ) { args ->
                val frame = args[0] as AudioFrame?
                val duration = args[1] as Long
                val analyzing = args[2] as Boolean
                val paused = args[3] as Boolean
                val anomalous = args[4] as Boolean
                val progress = args[5] as Float
                val calibrated = args[6] as Boolean

                _state.update {
                    it.copy(
                        currentFrame = frame,
                        durationText = AnalyzerProcessor.formatDuration(duration),
                        currentDurationMillis = duration,
                        isAnalyzing = analyzing,
                        isPaused = paused,
                        isAnomalous = anomalous,
                        isCalibrated = calibrated,
                        calibrationProgress = progress,
                        timelineMarkers = timelineMarkers.toList()
                    )
                }
            }.collect()
        }

        // Notes observer for current session
        scope.launch {
            _currentSessionIdFlow
                .flatMapLatest { sessionId ->
                    if (sessionId != null) {
                        combine(
                            historyRepository.getNotesForSession(sessionId),
                            preferencesRepository.markerShape
                        ) { notes, shape ->
                            notes.map {
                                SessionNoteUiModel(
                                    id = it.id,
                                    timestampMillis = it.timestamp,
                                    timestampText = AnalyzerProcessor.formatDuration(it.timestamp),
                                    text = it.text,
                                    markerColor = it.markerColor?.let { colorName ->
                                        try {
                                            ColorToken.valueOf(colorName)
                                        } catch (_: Exception) {
                                            null
                                        }
                                    },
                                    markerShape = if (it.markerColor != null) shape else null
                                )
                            }
                        }
                    } else {
                        flowOf(emptyList())
                    }
                }
                .collect { uiNotes ->
                    _state.update { it.copy(notes = uiNotes) }
                }
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
            AnalyzerProcessor.findClosestFrame(frameHistory, seekPos)
        } else {
            currentState.currentFrame
        }

        if (isPaused) {
            lastInterpretation = null
            interpretationTimestamp = 0
        }

        val (targetJitter, targetPitch, targetRms) = AnalyzerProcessor.calculateNormalizedMetrics(
            activeFrame
        )
        val rawStress = activeFrame?.stressScore ?: 0f

        smoothedJitter = AnalyzerProcessor.applyEmaSmoothing(targetJitter, smoothedJitter, isPaused)
        smoothedPitch = AnalyzerProcessor.applyEmaSmoothing(targetPitch, smoothedPitch, isPaused)
        smoothedRms = AnalyzerProcessor.applyEmaSmoothing(targetRms, smoothedRms, isPaused)

        val stressAlpha = if (isPaused) 0.40f else 0.25f
        smoothedStress = (rawStress * stressAlpha) + (smoothedStress * (1f - stressAlpha))

        val currentInterpretation =
            AnalyzerProcessor.determineInterpretation(smoothedJitter, smoothedPitch, smoothedRms)
        val now = application.poligraf.engine.utils.nowAsEpochMilliseconds()

        val finalInterpretation = if (currentInterpretation != null) {
            lastInterpretation = currentInterpretation
            interpretationTimestamp = now
            currentInterpretation
        } else if (!isPaused && lastInterpretation != null && (now - interpretationTimestamp) < AudioConstants.INTERPRETATION_STICKY_MS) {
            lastInterpretation
        } else {
            lastInterpretation = null
            null
        }

        val isAnomalous =
            if (isPaused) (activeFrame?.isAnomaly ?: false) else ((activeFrame?.isAnomaly
                ?: false) || smoothedStress > 0.30f)

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

    fun onAppear() {
        scope.launch {
            val isAnalyzing = repository.isAnalyzing.value
            val draft = repository.getActiveDraft()

            if (isAnalyzing) {
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
        frames.forEach { frame ->
            val lastMarkerTime = timelineMarkers.lastOrNull()?.timestampMillis ?: 0L
            AnalyzerProcessor.createAnomalyMarker(frame, currentMarkerShape, lastMarkerTime)?.let {
                timelineMarkers.add(it)
            }
        }
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
            smoothedJitter = 0f
            smoothedPitch = 0f
            smoothedRms = 0f
            smoothedStress = 0f

            currentSessionId = repository.startAnalysis(defaultTitle)
        }
    }

    fun onPauseResume() {
        if (repository.isPaused.value) {
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

        scope.launch {
            repository.stopAnalysis(save)
            if (save && sessionId != null) {
                _navigateToDetail.emit(sessionId)
            }
        }
    }

    fun onAction(action: Any) {
        when (action) {
            is AnalyzingAction.Save -> onStop(true)
            is AnalyzingAction.Delete -> onStop(false)
            else -> {}
        }
    }

    fun onBack() {
    }

    fun onSkinChange(skin: AnalyzerSkin) {
        preferencesRepository.setDefaultSkin(skin)
        _state.update { it.copy(currentSkin = skin) }
    }

    fun onNotesChange(notes: String) {
        _state.update { it.copy(currentNoteText = notes) }
    }

    fun onAddNote() {
        val text = _state.value.currentNoteText
        if (text.isBlank()) return

        val sessionId = currentSessionId ?: return
        val timestamp = _state.value.seekPositionMillis ?: _state.value.currentDurationMillis

        val associatedMarker = timelineMarkers.find {
            kotlin.math.abs(it.timestampMillis - timestamp) < 500
        }

        scope.launch {
            historyRepository.addNote(
                sessionId = sessionId,
                timestamp = timestamp,
                text = text,
                markerColor = associatedMarker?.colorToken?.name,
                markerShape = associatedMarker?.shape?.name
            )
            _state.update { it.copy(currentNoteText = "") }
        }
    }

    fun onDeleteNote(noteId: String) {
        scope.launch {
            historyRepository.deleteNote(noteId)
        }
    }
}
