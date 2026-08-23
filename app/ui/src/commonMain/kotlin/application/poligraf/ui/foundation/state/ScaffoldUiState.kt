package application.poligraf.ui.foundation.state

import androidx.compose.runtime.Stable
import application.poligraf.ui.foundation.models.LayoutConfig
import application.poligraf.ui.foundation.models.AppBackground
import application.poligraf.ui.foundation.models.AppToolbar

@Stable
interface ScaffoldUiState {
    val background: AppBackground
    val toolbar: AppToolbar?
    val layoutConfig: LayoutConfig
}
