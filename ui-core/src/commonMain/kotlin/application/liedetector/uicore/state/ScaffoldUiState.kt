package application.liedetector.uicore.state

import androidx.compose.runtime.Stable
import application.liedetector.uicore.models.LayoutConfig
import application.liedetector.uicore.widgets.AppBackground
import application.liedetector.uicore.widgets.UiWidget

@Stable
interface ScaffoldUiState {
    val background: AppBackground
    val toolbar: UiWidget.AppToolbar?
    val layoutConfig: LayoutConfig
}
