package application.liedetector.presentation.main

import androidx.compose.runtime.Stable
import application.liedetector.uicore.models.LayoutConfig
import application.liedetector.uicore.state.ScaffoldUiState
import application.liedetector.uicore.theme.tokens.StringToken
import application.liedetector.uicore.widgets.AppBackground
import application.liedetector.uicore.widgets.UiWidget
import application.liedetector.engine.config.AppConfig

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
