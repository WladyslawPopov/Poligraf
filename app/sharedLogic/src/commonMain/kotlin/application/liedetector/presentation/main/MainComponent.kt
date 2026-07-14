package application.liedetector.presentation.main

import androidx.compose.runtime.Stable
import application.liedetector.data.user.UserRepository
import application.liedetector.models.KmpResult
import application.liedetector.navigation.NavigationContext
import application.liedetector.presentation.base.BaseViewModel
import application.liedetector.uiwidgets.models.WidgetDto
import application.liedetector.uiwidgets.models.WidgetAction
import application.liedetector.uiwidgets.states.TopBarUiState
import application.liedetector.uicore.theme.BackgroundVisualizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.coroutineScope
import application.liedetector.engine.utils.watcher.asWatcher

@Stable
class MainComponent(
    val context: NavigationContext,
    val viewModel: MainViewModel,
    val backgroundVisualizer: BackgroundVisualizer
) {
    val stateWatcher = viewModel.state.asWatcher(context.lifecycle.coroutineScope)
    val backgroundWatcher = backgroundVisualizer.state.asWatcher(context.lifecycle.coroutineScope)

    fun onAction(action: WidgetAction) {
        viewModel.onWidgetAction(action)
    }
}

@Stable
data class MainState(
    val widgets: List<WidgetDto> = emptyList(),
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
                loadContent()
            } else if (authResult is KmpResult.Error) {
                _state.value = _state.value.copy(error = "Auth Error: ${authResult.throwable.message}")
            }
        })
    }

    fun loadContent() {
        launchSafe(
            block = {
                val result = userRepository.getMainScreen()
                if (result is KmpResult.Success) {
                    _state.value = _state.value.copy(widgets = result.data, error = null)
                } else if (result is KmpResult.Error) {
                    _state.value = _state.value.copy(error = "Server Unreachable", widgets = emptyList())
                }
            }
        )
    }

    fun onWidgetAction(action: WidgetAction) {
        println("Action triggered: $action")
    }
}
