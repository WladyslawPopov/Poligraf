package application.poligraf.ui.features.analyzer.state

import androidx.compose.runtime.Stable
import application.poligraf.domain.analyzer.model.AudioFrame
import application.poligraf.domain.analyzer.types.AnalyzerMode
import application.poligraf.domain.analyzer.types.AnalyzerSkin
import application.poligraf.domain.analyzer.types.DominantMetric
import application.poligraf.domain.analyzer.types.SignalLevel
import application.poligraf.domain.history.model.Session
import application.poligraf.ui.features.analyzer.models.AnalyzerMarker
import application.poligraf.ui.features.analyzer.models.SessionNoteUiModel
import application.poligraf.ui.foundation.models.AppBackground
import application.poligraf.ui.foundation.models.AppToolbar
import application.poligraf.ui.foundation.models.LayoutConfig
import application.poligraf.ui.foundation.state.ScaffoldUiState
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.StringToken

@Stable
data class AnalyzerState(
    override val background: AppBackground = AppBackground.Solid(),
    override val toolbar: AppToolbar? = null,
    override val layoutConfig: LayoutConfig = LayoutConfig(),
    val mode: AnalyzerMode = AnalyzerMode.LIVE,
    val displayFrame: AudioFrame? = null,
    val durationText: String = "00:00",
    val isAnalyzing: Boolean = false,
    val isPaused: Boolean = false,
    val isProcessing: Boolean = false,
    val isReadOnly: Boolean = false,
    val currentSkin: AnalyzerSkin = AnalyzerSkin.STATE_MAP,
    val jitterLevel: Float = 0f,
    val pitchLevel: Float = 0f,
    val rmsLevel: Float = 0f,
    val signalLevel: SignalLevel = SignalLevel.NONE,
    val dominantMetric: DominantMetric? = null,
    val timelineMarkers: List<AnalyzerMarker> = emptyList(),
    val notes: List<SessionNoteUiModel> = emptyList(),
    val currentNoteText: String = "",
    val currentDurationMillis: Long = 0,
    val seekPositionMillis: Long? = null,
    val activeInterpretation: StringToken? = null,

    // Review / History details (when mode == AnalyzerMode.REVIEW)
    val session: Session? = null,
    val anomalyCount: Int = 0,
    val averageConfidence: Float = 1.0f,
    val volatilityStatus: StringToken = StringToken.VOLATILITY_LOW,
    val volatilityColor: ColorToken = ColorToken.STATE_SUCCESS,
    val conclusionText: StringToken = StringToken.CONCLUSION_POSITIVE,
    val conclusionColor: ColorToken = ColorToken.STATE_SUCCESS,
    val isTitleEditing: Boolean = false,
    val isSaving: Boolean = false,
) : ScaffoldUiState
