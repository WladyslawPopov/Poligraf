package application.liedetector.presentation.main

import androidx.compose.runtime.Stable
import application.liedetector.data.user.UserRepository
import application.liedetector.models.KmpResult
import application.liedetector.navigation.AppNavigation
import application.liedetector.presentation.base.BaseViewModel
import application.liedetector.uiwidgets.models.UiWidget
import application.liedetector.uicore.state.TopBarUiState
import application.liedetector.uicore.theme.StringToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.coroutineScope
import application.liedetector.engine.component.ComponentContext
import application.liedetector.engine.utils.watcher.asWatcher
import application.liedetector.uicore.types.WidgetAction

@Stable
class MainComponent(
    val context: ComponentContext,
    val viewModel: MainViewModel
) {
    val stateWatcher = viewModel.state.asWatcher(context.lifecycle.coroutineScope)

    fun onAction(action: WidgetAction) {
        viewModel.onWidgetAction(action)
    }
    
    fun retry() {
        viewModel.loadContent()
    }
}

@Stable
data class MainState(
    val widgets: List<UiWidget> = emptyList(),
    val topBarState: TopBarUiState = TopBarUiState(titleToken = StringToken.APP_NAME),
    val errorRaw: String? = null,
    val errorToken: StringToken? = null
)

class MainViewModel(
    private val userRepository: UserRepository,
    private val navigation: AppNavigation
) : BaseViewModel() {
    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state.asStateFlow()

    init {
        startAuthAndLoad()
    }

    private fun startAuthAndLoad() {
        launchSafe(block = {
            println("MAIN: Starting Anonymous Auth...")
            val authResult = userRepository.loginAnonymously()
            if (authResult is KmpResult.Success) {
                println("MAIN: Auth Success! Requesting content...")
                loadContent()
            } else if (authResult is KmpResult.Error) {
                _state.value = _state.value.copy(errorRaw = "Auth Error: ${authResult.throwable.message}")
            }
        })
    }

    fun loadContent() {
        _state.value = _state.value.copy(errorRaw = null, errorToken = null) // Clear error before retry
        launchSafe(
            block = {
                val result = userRepository.getMainScreen()
                if (result is KmpResult.Success) {
                    println("MAIN: Content loaded successfully from SERVER")
                    _state.value = _state.value.copy(widgets = result.data, errorRaw = null, errorToken = null)
                } else if (result is KmpResult.Error) {
                    println("MAIN: Server Error: ${result.throwable.message}")
                    _state.value = _state.value.copy(errorToken = StringToken.ERROR_SERVER_TITLE, widgets = emptyList())
                }
            }
        )
    }

    fun onWidgetAction(action: WidgetAction) {
        println("Action triggered: $action")
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
