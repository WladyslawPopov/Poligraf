package application.liedetector.presentation.debug.data

import androidx.compose.runtime.Stable
import application.liedetector.uicore.state.ScaffoldUiState
import application.liedetector.uicore.widgets.AppBackground
import application.liedetector.uicore.widgets.UiWidget

@Stable
data class DebugState(
    override val background: AppBackground = AppBackground.Solid(),
    override val toolbar: UiWidget.AppToolbar? = null,
    val selectedTab: DebugTab = DebugTab.STATES,
    val widgets: List<UiWidget> = emptyList()
) : ScaffoldUiState
