package application.liedetector.presentation.main

import androidx.compose.runtime.Stable
import application.liedetector.uicore.state.ScaffoldUiState
import application.liedetector.uicore.theme.tokens.StringToken
import application.liedetector.uicore.widgets.AppBackground
import application.liedetector.uicore.widgets.UiWidget

@Stable
data class MainState(
    override val background: AppBackground = AppBackground.AnimatedScales(),
    override val toolbar: UiWidget.AppToolbar? = null,
    val welcomeWidget: UiWidget.WelcomeText? = null,
    val widgets: List<UiWidget> = emptyList(),
    val errorRaw: String? = null,
    val errorToken: StringToken? = null
) : ScaffoldUiState
