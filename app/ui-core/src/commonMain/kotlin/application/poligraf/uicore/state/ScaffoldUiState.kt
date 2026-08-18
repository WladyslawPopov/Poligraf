package application.poligraf.uicore.state

import androidx.compose.runtime.Stable
import application.poligraf.uicore.models.LayoutConfig
import application.poligraf.uicore.widgets.AppBackground
import application.poligraf.uicore.widgets.UiWidget

@Stable
interface ScaffoldUiState {
    val background: AppBackground
    val toolbar: UiWidget.AppToolbar?
    val layoutConfig: LayoutConfig
}
