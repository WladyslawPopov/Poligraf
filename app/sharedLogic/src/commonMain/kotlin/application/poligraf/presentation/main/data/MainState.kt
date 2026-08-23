package application.poligraf.presentation.main.data

import androidx.compose.runtime.Stable
import application.poligraf.engine.config.AppConfig
import application.poligraf.ui.foundation.models.LayoutConfig
import application.poligraf.ui.foundation.state.ScaffoldUiState
import application.poligraf.ui.foundation.models.AppBackground
import application.poligraf.ui.foundation.models.AppToolbar
import application.poligraf.ui.foundation.models.UiWidget

@Stable
data class MainState(
    override val background: AppBackground = AppBackground.AnimatedScales(),
    override val toolbar: AppToolbar? = null,
    override val layoutConfig: LayoutConfig = LayoutConfig(),
    val appConfig: AppConfig? = null,
    val bottomSheetState: Boolean = false,
    val bottomSheetContent: MainBottomSheetContent = MainBottomSheetContent.NONE,
    val widgets: List<UiWidget> = emptyList()
) : ScaffoldUiState

enum class MainBottomSheetContent {
    NONE,
    SETTINGS,
    ANALYZER
}
