package application.poligraf.presentation.main.data

import androidx.compose.runtime.Stable
import application.poligraf.engine.config.AppConfig
import application.poligraf.ui.features.main.models.MainAnalyzeBtnModel
import application.poligraf.ui.features.main.models.MainWelcomeModel
import application.poligraf.ui.foundation.models.AppBackground
import application.poligraf.ui.foundation.models.AppToolbar
import application.poligraf.ui.foundation.models.LayoutConfig
import application.poligraf.ui.foundation.state.ScaffoldUiState

@Stable
data class MainState(
    override val background: AppBackground = AppBackground.AnimatedScales(),
    override val toolbar: AppToolbar? = null,
    override val layoutConfig: LayoutConfig = LayoutConfig(),
    val appConfig: AppConfig? = null,
    val bottomSheetState: Boolean = false,
    val welcomeWidget: MainWelcomeModel? = null,
    val analyzeBtn: MainAnalyzeBtnModel? = null,
) : ScaffoldUiState
