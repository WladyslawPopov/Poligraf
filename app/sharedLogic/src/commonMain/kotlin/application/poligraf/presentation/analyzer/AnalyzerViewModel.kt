package application.poligraf.presentation.analyzer

import androidx.compose.runtime.Stable
import application.poligraf.data.analyzer.dsp.AnalyzerProcessor
import application.poligraf.domain.analyzer.repository.AnalyzerRepository
import application.poligraf.domain.analyzer.types.AnalyzerMode
import application.poligraf.domain.analyzer.types.AnalyzerSkin
import application.poligraf.domain.history.repository.HistoryRepository
import application.poligraf.domain.preferences.repository.PreferencesRepository
import application.poligraf.presentation.analyzer.logic.AnalyzerSessionController
import application.poligraf.presentation.analyzer.logic.AnalyzerUiMapper
import application.poligraf.presentation.base.BaseViewModel
import application.poligraf.ui.features.analyzer.actions.AnalyzingAction
import application.poligraf.ui.features.analyzer.models.SessionNoteUiModel
import application.poligraf.ui.features.analyzer.state.AnalyzerState
import application.poligraf.ui.foundation.models.AppToolbar
import application.poligraf.ui.foundation.models.ToolbarAction
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.IconToken
import application.poligraf.ui.theme.tokens.StringToken
import kotlinx.coroutines.Dispatchers
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
    initialSessionId: String? = null,
    private val repository: AnalyzerRepository,
    private val historyRepository: HistoryRepository,
    val preferencesRepository: PreferencesRepository,
    private val navigateBack: () -> Unit = {},
) : BaseViewModel() {

    private val isReviewMode = initialSessionId != null

    private val _state = MutableStateFlow(
        if (isReviewMode) {
            AnalyzerState(
                mode = AnalyzerMode.REVIEW,
                isReadOnly = true,
                isPaused = true,
                toolbar = AppToolbar(
                    titleToken = StringToken.HISTORY_DETAIL_TITLE,
                    backgroundColor = ColorToken.SURFACE_BACKGROUND,
                    contentColor = ColorToken.TEXT_PRIMARY
                )
            )
        } else {
            AnalyzerState(
                mode = AnalyzerMode.LIVE,
                isReadOnly = false,
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
        }
    )
    val state = _state.asStateFlow()

    private val _navigateToDetail = MutableSharedFlow<String>()
    val navigateToDetail = _navigateToDetail.asSharedFlow()

    private val controller = AnalyzerSessionController()

    private val _currentSessionIdFlow = MutableStateFlow(initialSessionId)
    private var currentSessionId: String? = initialSessionId
        set(value) {
            field = value
            _currentSessionIdFlow.value = value
        }

    init {
        // Observe preferences (marker shape & skin)
        scope.launch {
            preferencesRepository.markerShapeFlow.collect { shape ->
                val markers = controller.setMarkerShape(shape)
                val updatedNotes = _state.value.notes.map {
                    if (it.markerColor != null) it.copy(markerShape = shape) else it
                }
                _state.update {
                    it.copy(
                        timelineMarkers = markers,
                        notes = updatedNotes
                    )
                }
            }
        }

        scope.launch {
            preferencesRepository.skinFlow.collect { skin ->
                _state.update { it.copy(currentSkin = skin) }
            }
        }

        if (isReviewMode) {
            initReviewMode(initialSessionId!!)
        } else {
            initLiveMode()
        }

        // Shared Notes observer for current session
        scope.launch {
            _currentSessionIdFlow
                .flatMapLatest { sessionId ->
                    if (sessionId != null) {
                        combine(
                            historyRepository.getNotesForSession(sessionId),
                            preferencesRepository.markerShapeFlow
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
    }

    private fun initReviewMode(sessionId: String) {
        setLoading(true)

        // Load session metadata
        scope.launch {
            historyRepository.getSessionById(sessionId).collect { session ->
                _state.update { it.copy(session = session) }
                if (session != null) {
                    _state.update {
                        it.copy(durationText = AnalyzerProcessor.formatDuration(session.duration))
                    }
                }
            }
        }

        // Load frames and markers for Review Mode (excluding initial warmup calibration)
        scope.launch(Dispatchers.Default) {
            try {
                val allFrames = repository.getFramesForSession(sessionId)
                val reviewFrames = allFrames.filter { it.timestamp >= 5000L }
                controller.loadFrames(reviewFrames)

                val domainMarkers = repository.getMarkersForSession(sessionId)
                val uiMarkers = controller.setDomainMarkers(domainMarkers)

                val fullCount = uiMarkers.count { it.isAnomaly }
                val halftoneCount = uiMarkers.count { !it.isAnomaly }
                val weightedScore = fullCount * 1.0f + halftoneCount * 0.35f

                val anomalyCount = uiMarkers.size
                val avgConfidence = 1.0f
                val lastTimestamp = reviewFrames.lastOrNull()?.timestamp ?: 0L

                val volatilityStatus =
                    AnalyzerUiMapper.determineVolatilityStatus(weightedScore, lastTimestamp)
                val volatilityColor = AnalyzerUiMapper.determineVolatilityColor(volatilityStatus)
                val conclusionText = AnalyzerUiMapper.determineConclusionText(
                    weightedScore,
                    lastTimestamp,
                    avgConfidence
                )
                val conclusionColor = AnalyzerUiMapper.determineConclusionColor(conclusionText)

                _state.update { s ->
                    s.copy(
                        timelineMarkers = uiMarkers,
                        currentDurationMillis = lastTimestamp,
                        durationText = AnalyzerProcessor.formatDuration(lastTimestamp),
                        anomalyCount = anomalyCount,
                        fullAnomalyCount = fullCount,
                        halftoneAnomalyCount = halftoneCount,
                        averageConfidence = avgConfidence,
                        volatilityStatus = volatilityStatus,
                        volatilityColor = volatilityColor,
                        conclusionText = conclusionText,
                        conclusionColor = conclusionColor,
                        seekPositionMillis = reviewFrames.firstOrNull()?.timestamp ?: 0L
                    )
                }

                updateReviewDisplayState()
            } finally {
                setLoading(false)
            }
        }

        // Seek & settings observer for review mode (seeking updates active frame display ONLY)
        scope.launch {
            _state.map { it.seekPositionMillis }
                .distinctUntilChanged()
                .collect {
                    updateReviewDisplayState()
                }
        }
    }

    private fun updateReviewDisplayState() {
        val seekPos = _state.value.seekPositionMillis ?: 0L
        val quantumWindow = preferencesRepository.quantumWindowFlow.value
        val sensitivity = preferencesRepository.sensitivityFlow.value

        val snapshot = controller.resolveDisplay(
            seekPos = seekPos,
            isPaused = true,
            liveFrame = null,
            smooth = false,
            quantumWindowMs = quantumWindow.millis,
            sensitivity = sensitivity
        )

        _state.update {
            it.copy(
                displayFrame = snapshot.displayFrame,
                jitterLevel = snapshot.jitterLevel,
                pitchLevel = snapshot.pitchLevel,
                rmsLevel = snapshot.rmsLevel,
                signalLevel = snapshot.signalLevel,
                dominantMetric = snapshot.dominantMetric,
                activeInterpretation = snapshot.activeInterpretation,
                primaryAlpha = snapshot.primaryAlpha,
                secondaryInterpretations = snapshot.secondaryInterpretations,
                secondaryInterpretationsWithAlpha = snapshot.secondaryInterpretationsWithAlpha,
            )
        }
    }


    private fun initLiveMode() {
        // Continuous history frame recording from SharedFlow
        scope.launch {
            repository.audioFrames.collect { frame ->
                controller.onLiveFrame(frame)
            }
        }

        // Observe immutable session markers emitted by Engine on Quantum Window completion
        scope.launch {
            repository.sessionMarkers.collect { domainMarkers ->
                val uiMarkers = controller.setDomainMarkers(domainMarkers)
                _state.update { it.copy(timelineMarkers = uiMarkers) }
            }
        }

        // Observe repository state for UI updates
        scope.launch {
            combine(
                repository.currentFrame,
                repository.durationMillis,
                repository.isAnalyzing,
                repository.isPaused,
            ) { args ->
                val duration = args[1] as Long
                val analyzing = args[2] as Boolean
                val paused = args[3] as Boolean

                _state.update {
                    it.copy(
                        durationText = AnalyzerProcessor.formatDuration(duration),
                        currentDurationMillis = duration,
                        isAnalyzing = analyzing,
                        isPaused = paused,
                    )
                }
            }.collect()
        }

        // Observe Engine's currentQuantumAnalysis emitted ONCE per $T$-second quantum flush
        scope.launch {
            repository.currentQuantumAnalysis.collect { quantumAnalysis ->
                if (!_state.value.isPaused) {
                    val activeDisplayStatus = AnalyzerUiMapper.mapStatusToToken(quantumAnalysis.primaryStatus)
                    val primaryAlpha = quantumAnalysis.primaryAlpha

                    val secondaryInterpretationsWithAlpha = AnalyzerUiMapper.mapStatusesToTokensWithAlpha(
                        quantumAnalysis.secondaryStatusesWithScores
                    ).filter { it.first != activeDisplayStatus }

                    val secondaryInterpretations = secondaryInterpretationsWithAlpha.map { it.first }

                    _state.update {
                        it.copy(
                            activeInterpretation = activeDisplayStatus,
                            primaryAlpha = primaryAlpha,
                            secondaryInterpretations = secondaryInterpretations,
                            secondaryInterpretationsWithAlpha = secondaryInterpretationsWithAlpha
                        )
                    }
                }
            }
        }

        // Display resolution reacts to the latest frame + seek/pause state + settings.
        scope.launch {
            val seekPaused = _state.map { it.seekPositionMillis to it.isPaused }
                .distinctUntilChanged()

            combine(
                repository.currentFrame,
                seekPaused,
                preferencesRepository.quantumWindowFlow,
                preferencesRepository.sensitivityFlow
            ) { frame, (seek, paused), quantumWindow, sensitivity ->
                val snapshot = controller.resolveDisplay(
                    seekPos = seek,
                    isPaused = paused,
                    liveFrame = frame,
                    quantumWindowMs = quantumWindow.millis,
                    sensitivity = sensitivity
                )
                _state.update {
                    if (paused || seek != null) {
                        it.copy(
                            displayFrame = snapshot.displayFrame,
                            jitterLevel = snapshot.jitterLevel,
                            pitchLevel = snapshot.pitchLevel,
                            rmsLevel = snapshot.rmsLevel,
                            signalLevel = snapshot.signalLevel,
                            dominantMetric = snapshot.dominantMetric,
                            activeInterpretation = snapshot.activeInterpretation,
                            primaryAlpha = snapshot.primaryAlpha,
                            secondaryInterpretations = snapshot.secondaryInterpretations,
                            secondaryInterpretationsWithAlpha = snapshot.secondaryInterpretationsWithAlpha,
                        )
                    } else {
                        it.copy(
                            displayFrame = snapshot.displayFrame,
                            jitterLevel = snapshot.jitterLevel,
                            pitchLevel = snapshot.pitchLevel,
                            rmsLevel = snapshot.rmsLevel,
                            signalLevel = snapshot.signalLevel,
                            dominantMetric = snapshot.dominantMetric,
                        )
                    }
                }
            }.collect()
        }
    }

    fun onAppear() {
        if (isReviewMode) return

        scope.launch {
            val isAnalyzing = repository.isAnalyzing.value
            val draft = repository.getActiveDraft()

            if (isAnalyzing) {
                if (draft != null) {
                    currentSessionId = draft.first
                    if (controller.frameHistory.isEmpty()) {
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
        controller.loadFrames(frames)
        _state.update { it.copy(timelineMarkers = controller.timelineMarkers) }
    }

    fun onSeek(positionMillis: Long?) {
        _state.update { it.copy(seekPositionMillis = positionMillis) }
    }

    fun onStart() {
        if (isReviewMode) return
        scope.launch {
            val count = historyRepository.getSessionCount()
            val defaultTitle = "Session #${count + 1}"
            controller.reset()
            _state.update { it.copy(timelineMarkers = emptyList()) }

            currentSessionId = repository.startAnalysis(defaultTitle)
        }
    }

    fun onPauseResume() {
        if (isReviewMode) return
        if (repository.isPaused.value) {
            _state.update { it.copy(seekPositionMillis = null) }
            repository.resumeAnalysis()
        } else {
            repository.pauseAnalysis()
        }
    }

    fun onStop(save: Boolean) {
        if (isReviewMode || _state.value.isProcessing) return

        val sessionId = currentSessionId
        if (save && sessionId == null) return

        _state.update { it.copy(isProcessing = true) }

        val markerCount = controller.timelineMarkers.size.toLong()

        scope.launch {
            repository.stopAnalysis(save, markerCount)
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
        navigateBack()
    }

    fun onSkinChange(skin: AnalyzerSkin) {
        preferencesRepository.setSkin(skin)
        _state.update { it.copy(currentSkin = skin) }
    }

    fun onNotesChange(notes: String) {
        _state.update { it.copy(currentNoteText = notes) }
    }

    fun onAddNote() {
        val text = _state.value.currentNoteText
        if (text.isBlank()) return

        val sessionId = currentSessionId ?: return
        val timestamp = _state.value.seekPositionMillis
            ?: if (isReviewMode) 0L else _state.value.currentDurationMillis

        val associatedMarker = controller.timelineMarkers.find {
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

    // Review Mode - Title & Metadata editing
    fun onTitleChange(title: String) {
        val currentSession = _state.value.session ?: return
        _state.update { it.copy(session = currentSession.copy(title = title)) }
    }

    fun toggleTitleEdit(isEditing: Boolean) {
        if (!isEditing) {
            onSaveMetadata()
        }
        _state.update { it.copy(isTitleEditing = isEditing) }
    }

    fun onSaveMetadata() {
        val sessionId = currentSessionId ?: return
        val session = _state.value.session ?: return
        scope.launch {
            _state.update { it.copy(isSaving = true) }
            historyRepository.updateSessionMetadata(
                id = sessionId,
                title = session.title,
                notes = session.notes
            )
            _state.update { it.copy(isSaving = false) }
        }
    }
}

