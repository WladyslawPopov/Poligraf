package application.liedetector.presentation.debug.data

import androidx.compose.runtime.Stable
import application.liedetector.uicore.widgets.UiWidget

@Stable
data class DebugState(
    val selectedTab: DebugTab = DebugTab.STATES,
    val widgets: List<UiWidget> = emptyList()
)
