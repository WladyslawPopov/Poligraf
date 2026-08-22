package application.poligraf.presentation.main

import application.poligraf.presentation.base.BaseViewModel
import application.poligraf.uicore.theme.tokens.ColorToken
import application.poligraf.uicore.actions.NavigationAction
import application.poligraf.uicore.actions.WidgetAction
import application.poligraf.uicore.widgets.AppBackground
import application.poligraf.uicore.widgets.UiWidget
import application.poligraf.engine.config.AppConfig
import application.poligraf.presentation.main.data.MainState
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainViewModel(
    private val appConfig: AppConfig,
    private val navigateToDebug: () -> Unit
) : BaseViewModel()
{
    private val _state = MutableStateFlow(
        MainState(
            appConfig = appConfig,
            background = AppBackground.AnimatedScales(
                baseColor = ColorToken.BACKGROUND,
                energyColor = ColorToken.ACCENT_ENERGY,
                particleColor = ColorToken.SURFACE_VARIANT,
                parallaxIntensity = 1.2f,
                blurRadius = 4.0f
            ),
            toolbar = UiWidget.AppToolbar(
                id = "main_toolbar",
                titleProvider = { it.common.appName },
                menuAction = NavigationAction.Menu,
                backgroundColor = ColorToken.BACKGROUND,
                contentColor = ColorToken.TEXT_PRIMARY
            )
        )
    )
    val state: StateFlow<MainState> = _state.asStateFlow()

    fun setDrawerOpen(isOpen: Boolean) {
        _state.update { it.copy(isDrawerOpen = isOpen) }
    }

    fun onWidgetAction(action: WidgetAction) {
        Napier.d { "Action triggered: $action" }
        when (action) {
            is NavigationAction.Menu -> setDrawerOpen(true)
            is NavigationAction.Settings -> navigateToDebug()
            else -> {}
        }
    }

    fun onDebugClicked() {
        navigateToDebug()
    }
    fun loadContent() {
        // Shared content loading if any
    }
}
