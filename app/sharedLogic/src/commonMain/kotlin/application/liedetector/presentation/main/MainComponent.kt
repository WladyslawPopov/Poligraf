package application.liedetector.presentation.main

import androidx.compose.runtime.Stable
import application.liedetector.data.user.UserRepository
import application.liedetector.models.KmpResult
import application.liedetector.navigation.NavigationContext
import application.liedetector.presentation.base.BaseViewModel
import application.liedetector.uiwidgets.models.UiWidget
import application.liedetector.uiwidgets.models.WidgetAction
import application.liedetector.uiwidgets.states.TopBarUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.coroutineScope
import application.liedetector.engine.utils.watcher.asWatcher

@Stable
class MainComponent(
    val context: NavigationContext,
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
    val topBarState: TopBarUiState = TopBarUiState(title = "Lie Detector"),
    val error: String? = null
)

class MainViewModel(private val userRepository: UserRepository) : BaseViewModel() {
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
                _state.value = _state.value.copy(error = "Auth Error: ${authResult.throwable.message}")
            }
        })
    }

    fun loadContent() {
        _state.value = _state.value.copy(error = null) // Clear error before retry
        launchSafe(
            block = {
                val result = userRepository.getMainScreen()
                if (result is KmpResult.Success) {
                    println("MAIN: Content loaded successfully from SERVER")
                    _state.value = _state.value.copy(widgets = result.data, error = null)
                } else if (result is KmpResult.Error) {
                    println("MAIN: Server Error: ${result.throwable.message}")
                    _state.value = _state.value.copy(error = "Server Unreachable (Click to Retry)", widgets = emptyList())
                }
            }
        )
    }

    fun onWidgetAction(action: WidgetAction) {
        println("Action triggered: $action")
    }
}
