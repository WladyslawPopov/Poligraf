package application.poligraf.presentation.debug.data

import androidx.compose.runtime.Stable
import application.poligraf.uicore.models.LayoutConfig
import application.poligraf.uicore.state.ScaffoldUiState
import application.poligraf.uicore.widgets.AppBackground
import application.poligraf.uicore.widgets.UiWidget

@Stable
data class DebugState(
    override val background: AppBackground = AppBackground.AnimatedScales(),
    override val toolbar: UiWidget.AppToolbar? = null,
    override val layoutConfig: LayoutConfig = LayoutConfig(isCentered = false),
    val selectedTab: DebugTab = DebugTab.STATES,
    val widgets: List<UiWidget> = emptyList()
) : ScaffoldUiState
