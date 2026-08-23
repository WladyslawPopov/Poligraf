package application.poligraf.presentation.debug.data

import androidx.compose.runtime.Stable
import application.poligraf.ui.foundation.models.LayoutConfig
import application.poligraf.ui.foundation.state.ScaffoldUiState
import application.poligraf.ui.foundation.models.AppBackground
import application.poligraf.ui.foundation.models.AppToolbar
import application.poligraf.ui.foundation.models.UiWidget

@Stable
data class DebugState(
    override val background: AppBackground = AppBackground.AnimatedScales(),
    override val toolbar: AppToolbar? = null,
    override val layoutConfig: LayoutConfig = LayoutConfig(isCentered = false),
    val selectedTab: DebugTab = DebugTab.STATES,
    val widgets: List<UiWidget> = emptyList()
) : ScaffoldUiState
