package application.poligraf.ui.foundation.state

import androidx.compose.runtime.Stable
import application.poligraf.domain.model.AudioFrame
import application.poligraf.domain.model.AnalyzerSkin
import application.poligraf.ui.foundation.models.AnalyzerMarker
import application.poligraf.ui.foundation.models.AppBackground
import application.poligraf.ui.foundation.models.AppToolbar
import application.poligraf.ui.foundation.models.LayoutConfig
import application.poligraf.ui.foundation.models.SessionNoteUiModel
import application.poligraf.ui.theme.tokens.StringToken

@Stable
data class AnalyzerState(
    override val background: AppBackground = AppBackground.Solid(),
    override val toolbar: AppToolbar? = null,
    override val layoutConfig: LayoutConfig = LayoutConfig(),
    val currentFrame: AudioFrame? = null,
    val durationText: String = "00:00",
    val isAnalyzing: Boolean = false,
    val isPaused: Boolean = false,
    val isCalibrated: Boolean = false,
    val isAnomalous: Boolean = false,
    val isDisplayAnomalous: Boolean = false,
    val isProcessing: Boolean = false,
    val isReadOnly: Boolean = false,
    val calibrationProgress: Float = 0f,
    val displayFrame: AudioFrame? = null,
    val currentSkin: AnalyzerSkin = AnalyzerSkin.STATE_MAP,
    val jitterLevel: Float = 0f,
    val pitchLevel: Float = 0f,
    val rmsLevel: Float = 0f,
    val timelineMarkers: List<AnalyzerMarker> = emptyList(),
    val notes: List<SessionNoteUiModel> = emptyList(),
    val currentNoteText: String = "",
    val currentDurationMillis: Long = 0,
    val seekPositionMillis: Long? = null,
    val activeInterpretation: StringToken? = null,
) : ScaffoldUiState
