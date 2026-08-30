package application.poligraf.presentation.history_detail

import androidx.compose.runtime.Stable
import application.poligraf.domain.model.AnalyzerSkin
import application.poligraf.domain.model.AudioFrame
import application.poligraf.domain.model.MarkerShape
import application.poligraf.domain.repository.AnalyzerRepository
import application.poligraf.domain.repository.HistoryRepository
import application.poligraf.domain.repository.PreferencesRepository
import application.poligraf.engine.dsp.AudioAnalyzer
import application.poligraf.engine.io.audio.AudioConstants
import application.poligraf.presentation.base.BaseViewModel
import application.poligraf.presentation.history_detail.data.HistoryDetailState
import application.poligraf.presentation.history_detail.data.SessionNoteUiModel
import application.poligraf.presentation.analyzer.logic.AnalyzerProcessor
import application.poligraf.ui.foundation.models.AnalyzerMarker
import application.poligraf.ui.foundation.models.AppToolbar
import application.poligraf.ui.foundation.state.AnalyzerState
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.StringToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Stable
class HistoryDetailViewModel(
    private val sessionId: String,
    private val historyRepository: HistoryRepository,
    private val analyzerRepository: AnalyzerRepository,
    private val preferencesRepository: PreferencesRepository,
    private val navigateBack: () -> Unit,
) : BaseViewModel() {

    private val _state = MutableStateFlow(
        HistoryDetailState(
            toolbar = AppToolbar(
                titleToken = StringToken.HISTORY_DETAIL_TITLE,
                backgroundColor = ColorToken.SURFACE_BACKGROUND,
                contentColor = ColorToken.TEXT_PRIMARY
            ),
            analyzerState = AnalyzerState(isReadOnly = true)
        )
    )
    val state = _state.asStateFlow()

    private val frameHistory = mutableListOf<AudioFrame>()
    private val timelineMarkers = mutableListOf<AnalyzerMarker>()
    private var currentMarkerShape: MarkerShape = MarkerShape.CIRCLE

    init {
        // Observe preferences
        scope.launch {
            preferencesRepository.markerShape.collect { shape ->
                currentMarkerShape = shape
                val updated = timelineMarkers.map { it.copy(shape = shape) }
                timelineMarkers.clear()
                timelineMarkers.addAll(updated)

                val updatedNotes = _state.value.notes.map {
                    if (it.markerColor != null) it.copy(markerShape = shape) else it
                }

                _state.update { s ->
                    s.copy(
                        notes = updatedNotes,
                        analyzerState = s.analyzerState.copy(
                            timelineMarkers = timelineMarkers.toList()
                        )
                    )
                }
            }
        }

        scope.launch {
            preferencesRepository.defaultSkin.collect { skin ->
                _state.update { s ->
                    s.copy(
                        analyzerState = s.analyzerState.copy(
                            currentSkin = skin
                        )
                    )
                }
            }
        }

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

        // Load frames and process summary with POST-SESSION NORMALIZATION
        scope.launch(Dispatchers.Default) {
            val rawFrames = analyzerRepository.getFramesForSession(sessionId)
            
            // Re-process for higher accuracy using full session context
            val globalBaseline = AudioAnalyzer.MovingBaseline(windowSize = rawFrames.size.coerceAtLeast(100))
            rawFrames.forEach { globalBaseline.add(it.rms, it.pitch, it.jitter) }

            val processedFrames = ArrayList<AudioFrame>(rawFrames.size)
            val markers = mutableListOf<AnalyzerMarker>()

            rawFrames.forEach { raw ->
                val result = AudioAnalyzer.calculateAdvancedAnalysis(
                    rms = raw.rms,
                    pitch = raw.pitch,
                    jitter = raw.jitter,
                    baseline = globalBaseline
                )
                
                val refinedFrame = raw.copy(
                    stressScore = result.stressScore,
                    jitterScore = result.jitterScore,
                    pitchScore = result.pitchScore,
                    rmsScore = result.rmsScore,
                    isAnomaly = result.isAnomaly,
                    confidence = result.confidence,
                    isCritical = result.isCritical
                )
                
                processedFrames.add(refinedFrame)
                
                val lastMarkerTime = markers.lastOrNull()?.timestampMillis ?: 0L
                AnalyzerProcessor.createAnomalyMarker(refinedFrame, currentMarkerShape, lastMarkerTime)?.let {
                    markers.add(it)
                }
            }

            frameHistory.clear()
            frameHistory.addAll(processedFrames)
            timelineMarkers.clear()
            timelineMarkers.addAll(markers)

            val anomalyCount = markers.count { it.isAnomaly }
            val avgConfidence = if (rawFrames.isNotEmpty()) rawFrames.map { it.confidence }.average().toFloat() else 0f

            _state.update { s ->
                val lastTimestamp = frameHistory.lastOrNull()?.timestamp ?: 0L
                s.copy(
                    analyzerState = s.analyzerState.copy(
                        timelineMarkers = markers,
                        currentDurationMillis = lastTimestamp,
                        durationText = AnalyzerProcessor.formatDuration(lastTimestamp)
                    ),
                    anomalyCount = anomalyCount,
                    averageConfidence = avgConfidence,
                    volatilityStatus = determineVolatilityStatus(anomalyCount),
                    volatilityColor = determineVolatilityColor(anomalyCount),
                    conclusionText = determineConclusionText(anomalyCount, avgConfidence),
                    conclusionColor = determineConclusionColor(anomalyCount, avgConfidence)
                )
            }

            // Initial display frame
            updateDisplayState()
        }

        // Seek observer
        scope.launch {
            _state.map { it.analyzerState.seekPositionMillis }
                .distinctUntilChanged()
                .collect {
                    updateDisplayState()
                }
        }

        // Notes observer
        scope.launch {
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
            }.collect { uiNotes ->
                _state.update { it.copy(notes = uiNotes) }
            }
        }
    }

    private fun updateDisplayState() {
        val currentState = _state.value.analyzerState
        val seekPos = currentState.seekPositionMillis ?: 0L

        val activeFrame = AnalyzerProcessor.findClosestFrame(frameHistory, seekPos) ?: frameHistory.firstOrNull()
        val (targetJitter, targetPitch, targetRms) = AnalyzerProcessor.calculateNormalizedMetrics(activeFrame)

        _state.update { s ->
            s.copy(
                analyzerState = s.analyzerState.copy(
                    displayFrame = activeFrame,
                    jitterLevel = targetJitter,
                    pitchLevel = targetPitch,
                    rmsLevel = targetRms,
                    isDisplayAnomalous = activeFrame?.isAnomaly ?: false,
                    isPaused = true 
                )
            )
        }
    }

    private fun determineVolatilityStatus(count: Int) = when {
        count <= 2 -> StringToken.VOLATILITY_LOW
        count <= 5 -> StringToken.VOLATILITY_MEDIUM
        else -> StringToken.VOLATILITY_HIGH
    }

    private fun determineVolatilityColor(count: Int) = when {
        count <= 2 -> ColorToken.STATE_SUCCESS
        count <= 5 -> ColorToken.STATE_WARNING
        else -> ColorToken.STATE_ERROR
    }

    private fun determineConclusionText(count: Int, confidence: Float): StringToken {
        if (confidence < 0.6f) return StringToken.RETRY // Or any "Unreliable" token
        return when {
            count <= 3 -> StringToken.CONCLUSION_POSITIVE
            count <= 6 -> StringToken.CONCLUSION_NEUTRAL
            else -> StringToken.CONCLUSION_NEGATIVE
        }
    }

    private fun determineConclusionColor(count: Int, confidence: Float): ColorToken {
        if (confidence < 0.6f) return ColorToken.STATE_ERROR
        return when {
            count <= 3 -> ColorToken.STATE_SUCCESS
            count <= 6 -> ColorToken.STATE_WARNING
            else -> ColorToken.STATE_ERROR
        }
    }

    fun onSeek(positionMillis: Long?) {
        _state.update { s ->
            s.copy(analyzerState = s.analyzerState.copy(seekPositionMillis = positionMillis))
        }
    }

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

    fun onNotesChange(notes: String) {
        _state.update { it.copy(currentNoteText = notes) }
    }

    fun onAddNote() {
        val text = _state.value.currentNoteText
        if (text.isBlank()) return

        val timestamp = _state.value.analyzerState.seekPositionMillis ?: 0L

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

    fun onSaveMetadata() {
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

    fun onSkinChange(skin: AnalyzerSkin) {
        preferencesRepository.setDefaultSkin(skin)
        _state.update { s ->
            s.copy(analyzerState = s.analyzerState.copy(currentSkin = skin))
        }
    }

    fun onBack() {
        navigateBack()
    }
}
