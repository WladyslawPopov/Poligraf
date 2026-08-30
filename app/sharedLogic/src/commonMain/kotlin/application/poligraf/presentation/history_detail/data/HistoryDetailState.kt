package application.poligraf.presentation.history_detail.data

import androidx.compose.runtime.Stable
import application.poligraf.domain.model.Session
import application.poligraf.ui.foundation.state.AnalyzerState
import application.poligraf.ui.foundation.models.AppBackground
import application.poligraf.ui.foundation.models.AppToolbar
import application.poligraf.ui.foundation.models.LayoutConfig
import application.poligraf.ui.foundation.state.ScaffoldUiState
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.StringToken

@Stable
data class HistoryDetailState(
    override val background: AppBackground = AppBackground.Solid(),
    override val toolbar: AppToolbar? = null,
    override val layoutConfig: LayoutConfig = LayoutConfig(),
    val session: Session? = null,
    val analyzerState: AnalyzerState = AnalyzerState(isReadOnly = true),
    
    // Summary
    val anomalyCount: Int = 0,
    val durationText: String = "00:00",
    val volatilityStatus: StringToken = StringToken.VOLATILITY_LOW,
    val volatilityColor: ColorToken = ColorToken.STATE_SUCCESS,
    
    // Conclusion
    val conclusionText: StringToken = StringToken.CONCLUSION_POSITIVE,
    val conclusionColor: ColorToken = ColorToken.STATE_SUCCESS,
    
    val notes: List<SessionNoteUiModel> = emptyList(),
    val currentNoteText: String = "",
    val averageConfidence: Float = 1.0f,
    
    val isTitleEditing: Boolean = false,
    val isSaving: Boolean = false
) : ScaffoldUiState
