package application.poligraf.presentation.main

import androidx.compose.runtime.Stable
import application.poligraf.uicore.models.LayoutConfig
import application.poligraf.uicore.state.ScaffoldUiState
import application.poligraf.uicore.theme.tokens.StringToken
import application.poligraf.uicore.widgets.AppBackground
import application.poligraf.uicore.widgets.UiWidget
import application.poligraf.engine.config.AppConfig

@Stable
data class MainState(
    override val background: AppBackground = AppBackground.AnimatedScales(),
    override val toolbar: UiWidget.AppToolbar? = null,
    override val layoutConfig: LayoutConfig = LayoutConfig(),
    val appConfig: AppConfig? = null,
    val welcomeWidget: UiWidget.WelcomeText? = null,
    val widgets: List<UiWidget> = emptyList(),
    val errorRaw: String? = null,
    val errorToken: StringToken? = null
) : ScaffoldUiState
