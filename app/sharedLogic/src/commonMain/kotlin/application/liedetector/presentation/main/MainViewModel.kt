package application.liedetector.presentation.main

import application.liedetector.data.user.UserRepository
import application.liedetector.models.KmpResult
import application.liedetector.navigation.AppNavigation
import application.liedetector.presentation.base.BaseViewModel
import application.liedetector.uicore.theme.StringToken
import application.liedetector.uicore.types.WidgetAction
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel(
    private val userRepository: UserRepository,
    private val navigation: AppNavigation
) : BaseViewModel()
{
    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state.asStateFlow()

    init {
        loadContent()
    }
    
    fun loadContent() {
        _state.value = _state.value.copy(errorRaw = null, errorToken = null) // Clear error before retry
        
        launchSafe(
            block = {
                val result = userRepository.getMainScreen()
                if (result is KmpResult.Success) {
                    Napier.d { "MAIN: Content loaded successfully from SERVER" }
                    _state.value = _state.value.copy(widgets = result.data, errorRaw = null, errorToken = null)
                } else if (result is KmpResult.Error) {
                    Napier.e(result.throwable) { "MAIN: Server Error" }
                    _state.value = _state.value.copy(errorToken = StringToken.ERROR_SERVER_TITLE, widgets = emptyList())
                }
            }
        )
    }

    fun onWidgetAction(action: WidgetAction) {
        Napier.d { "Action triggered: $action" }
        when (action) {
            WidgetAction.OPEN_HISTORY -> {
                navigation.openMain() // Change later to history
            }
            WidgetAction.OPEN_SETTINGS -> {
                navigation.toggleDrawer()
            }
            else -> {}
        }
    }
}
