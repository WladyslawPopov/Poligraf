package application.poligraf.presentation.history.data

import androidx.compose.runtime.Stable
import application.poligraf.ui.foundation.models.AppBackground
import application.poligraf.ui.foundation.models.AppToolbar
import application.poligraf.ui.foundation.models.LayoutConfig
import application.poligraf.ui.foundation.state.ScaffoldUiState

@Stable
data class HistoryState(
    override val background: AppBackground = AppBackground.Solid(),
    override val toolbar: AppToolbar? = null,
    override val layoutConfig: LayoutConfig = LayoutConfig(),
    val sessions: List<SessionUiModel> = emptyList(),
    val isLoading: Boolean = false
) : ScaffoldUiState
