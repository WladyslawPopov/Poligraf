package application.poligraf.presentation.history_detail

import androidx.compose.runtime.Stable
import application.poligraf.domain.model.AudioFrame
import application.poligraf.domain.repository.AnalyzerRepository
import application.poligraf.domain.repository.HistoryRepository
import application.poligraf.engine.io.audio.AudioConstants
import application.poligraf.engine.models.AnalyzerSkin
import application.poligraf.engine.models.MarkerShape
import application.poligraf.engine.settings.PreferenceManager
import application.poligraf.presentation.base.BaseViewModel
import application.poligraf.presentation.history_detail.data.HistoryDetailState
import application.poligraf.ui.foundation.models.AnalyzerMarker
import application.poligraf.ui.foundation.models.AppToolbar
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.StringToken
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@Stable
class HistoryDetailViewModel(
    private val sessionId: String,
    private val historyRepository: HistoryRepository,
    private val analyzerRepository: AnalyzerRepository,
    private val preferenceManager: PreferenceManager,
    private val navigateBack: () -> Unit,
) : BaseViewModel() {

    private val _state = MutableStateFlow(
        HistoryDetailState(
            toolbar = AppToolbar(
                titleToken = StringToken.HISTORY_DETAIL_TITLE,
                backgroundColor = ColorToken.SURFACE_BACKGROUND,
                contentColor = ColorToken.TEXT_PRIMARY
            )
        )
    )
    val state = _state.asStateFlow()

    private val frameHistory = mutableListOf<AudioFrame>()
    private val timelineMarkers = mutableListOf<AnalyzerMarker>()
    private var currentMarkerShape: MarkerShape = MarkerShape.CIRCLE

    init {
        // Observe preferences
        scope.launch {
            preferenceManager.markerShape.collect { shape ->
                currentMarkerShape = shape
                val updated = timelineMarkers.map { it.copy(shape = shape) }
                timelineMarkers.clear()
                timelineMarkers.addAll(updated)
                _state.update {
                    it.copy(
                        analyzerState = it.analyzerState.copy(
                            timelineMarkers = timelineMarkers.toList()
                        )
                    )
                }
            }
        }

        scope.launch {
            preferenceManager.defaultSkin.collect { skin ->
                _state.update {
                    it.copy(
                        analyzerState = it.analyzerState.copy(
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
                        it.copy(durationText = formatDuration(session.duration))
                    }
                }
            }
        }

        // Load frames and process summary
        scope.launch {
            val frames = analyzerRepository.getFramesForSession(sessionId)
            frameHistory.clear()
            frameHistory.addAll(frames)

            timelineMarkers.clear()
            frames.forEach { processAnomalyMarker(it) }

            val markers = timelineMarkers.toList()
            val anomalyCount = markers.count { it.isAnomaly }

            _state.update {
                it.copy(
                    analyzerState = it.analyzerState.copy(
                        timelineMarkers = markers,
                        currentDurationMillis = frameHistory.lastOrNull()?.timestamp ?: 0L,
                        durationText = formatDuration(frameHistory.lastOrNull()?.timestamp ?: 0L)
                    ),
                    anomalyCount = anomalyCount,
                    volatilityStatus = determineVolatilityStatus(anomalyCount),
                    volatilityColor = determineVolatilityColor(anomalyCount),
                    conclusionText = determineConclusionText(anomalyCount),
                    conclusionColor = determineConclusionColor(anomalyCount)
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
    }

    private fun updateDisplayState() {
        val currentState = _state.value.analyzerState
        val seekPos = currentState.seekPositionMillis ?: 0L

        val activeFrame = findClosestFrame(seekPos) ?: frameHistory.firstOrNull()

        val rawJitter = activeFrame?.jitter ?: 0f
        val rawPitch = activeFrame?.pitch ?: 0f
        val rawRms = activeFrame?.rms ?: 0f

        // Normalize for UI
        val targetJitter = (rawJitter / AudioConstants.MAX_EXPECTED_JITTER).coerceIn(0f, 1f)
        val targetPitch = if (rawPitch > 50f) {
            ((rawPitch - AudioConstants.MIN_EXPECTED_PITCH) / (AudioConstants.MAX_EXPECTED_PITCH - AudioConstants.MIN_EXPECTED_PITCH))
                .coerceIn(0f, 1f)
        } else 0f
        val targetRms = (rawRms * AudioConstants.RMS_VISUAL_AMPLIFIER).coerceIn(0f, 1f)

        _state.update {
            it.copy(
                analyzerState = it.analyzerState.copy(
                    displayFrame = activeFrame,
                    jitterLevel = targetJitter,
                    pitchLevel = targetPitch,
                    rmsLevel = targetRms,
                    isDisplayAnomalous = activeFrame?.isAnomaly ?: false,
                    isPaused = true // Ensure UI treats it as static
                )
            )
        }
    }

    private fun findClosestFrame(seekPos: Long): AudioFrame? {
        if (frameHistory.isEmpty()) return null
        
        // Find the frame with timestamp closest to seekPos
        return frameHistory.minByOrNull { kotlin.math.abs(it.timestamp - seekPos) }
    }

    private fun processAnomalyMarker(frame: AudioFrame) {
        if (frame.isAnomaly) {
            val dominantColor = when {
                frame.jitter > 30f -> ColorToken.CHART_JITTER
                frame.pitch > 200f -> ColorToken.CHART_PITCH
                else -> ColorToken.CHART_RMS
            }

            val marker = AnalyzerMarker(
                id = "m_${frame.timestamp}",
                timestampMillis = frame.timestamp,
                timestampText = formatDuration(frame.timestamp),
                colorToken = dominantColor,
                isAnomaly = true,
                shape = currentMarkerShape
            )
            if (timelineMarkers.none { it.timestampText == marker.timestampText }) {
                timelineMarkers.add(marker)
            }
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

    private fun determineConclusionText(count: Int) = when {
        count <= 3 -> StringToken.CONCLUSION_POSITIVE
        count <= 6 -> StringToken.CONCLUSION_NEUTRAL
        else -> StringToken.CONCLUSION_NEGATIVE
    }

    private fun determineConclusionColor(count: Int) = when {
        count <= 3 -> ColorToken.STATE_SUCCESS
        count <= 6 -> ColorToken.STATE_WARNING
        else -> ColorToken.STATE_ERROR
    }

    fun onSeek(positionMillis: Long?) {
        _state.update {
            it.copy(analyzerState = it.analyzerState.copy(seekPositionMillis = positionMillis))
        }
    }

    fun onTitleChange(title: String) {
        val currentSession = _state.value.session ?: return
        _state.update { it.copy(session = currentSession.copy(title = title)) }
    }

    fun onNotesChange(notes: String) {
        val currentSession = _state.value.session ?: return
        _state.update { it.copy(session = currentSession.copy(notes = notes)) }
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
        _state.update {
            it.copy(analyzerState = it.analyzerState.copy(currentSkin = skin))
        }
    }

    fun onBack() {
        navigateBack()
    }

    private fun formatDuration(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    }
}
