package application.liedetector.presentation.debug

import androidx.compose.runtime.Stable
import application.liedetector.navigation.NavigationContext
import application.liedetector.navigation.AppNavigation
import application.liedetector.presentation.base.BaseViewModel
import application.liedetector.uiwidgets.models.UiWidget
import application.liedetector.uiwidgets.models.WidgetAction
import application.liedetector.uicore.state.*
import application.liedetector.uicore.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.coroutineScope
import application.liedetector.engine.utils.watcher.asWatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Stable
class DebugComponent(
    val context: NavigationContext,
    val viewModel: DebugViewModel
) {
    val stateWatcher = viewModel.state.asWatcher(context.lifecycle.coroutineScope)

    fun onAction(action: WidgetAction) {
        viewModel.onWidgetAction(action)
    }
    
    fun setTab(tab: DebugTab) {
        viewModel.setTab(tab)
    }
}

enum class DebugTab {
    STATES, WIDGETS, LABS
}

@Stable
data class DebugState(
    val selectedTab: DebugTab = DebugTab.STATES,
    val widgets: List<UiWidget> = emptyList()
)

class DebugViewModel(private val navigation: AppNavigation) : BaseViewModel() {
    private val _state = MutableStateFlow(DebugState())
    val state: StateFlow<DebugState> = _state.asStateFlow()

    init {
        loadMockWidgets()
    }

    fun setTab(tab: DebugTab) {
        _state.value = _state.value.copy(selectedTab = tab)
    }

    private fun loadMockWidgets() {
        val mocks = listOf(
            UiWidget.Header(
                id = "h1",
                titleToken = StringToken.WELCOME_TITLE,
                subtitleToken = StringToken.WELCOME_SUBTITLE
            ),
            UiWidget.VerdictCard(
                id = "v1",
                verdictToken = StringToken.WELCOME_TITLE, // Reuse for test
                score = 85,
                colorToken = ColorToken.TRUTH
            ),
            UiWidget.VerdictCard(
                id = "v2",
                verdictToken = StringToken.ERROR_UNKNOWN_TITLE,
                score = 42,
                colorToken = ColorToken.STRESS
            ),
            UiWidget.AcousticGraph(
                id = "g1",
                points = listOf(0.1f, 0.5f, 0.3f, 0.8f, 0.4f),
                colorToken = ColorToken.ACCENT_ENERGY
            ),
            UiWidget.MicrophoneButton(
                id = "m1",
                action = WidgetAction.START_RECORDING
            ),
            UiWidget.StandardButton(
                id = "b1",
                textToken = StringToken.START_INVESTIGATION,
                action = WidgetAction.OPEN_HISTORY
            )
        )
        _state.value = _state.value.copy(widgets = mocks)
    }

    fun onWidgetAction(action: WidgetAction) {
        when (action) {
            WidgetAction.OPEN_HISTORY -> {
                navigation.openMain()
            }
            WidgetAction.DEBUG_TRIGGER_LOADING -> {
                scope.launch {
                    setLoading(true)
                    delay(2000)
                    setLoading(false)
                }
            }
            WidgetAction.DEBUG_TRIGGER_ERROR_BLOCKING -> {
                setManualError(ErrorType.SERVER_UNAVAILABLE)
            }
            WidgetAction.DEBUG_TRIGGER_ERROR_NON_BLOCKING -> {
                showToast(StringToken.ERROR_UNKNOWN_TITLE, ToastType.ERROR)
            }
            WidgetAction.DEBUG_TRIGGER_SUCCESS_TOAST -> {
                showToast(StringToken.TOAST_AUTH_SUCCESS, ToastType.SUCCESS)
            }
            else -> {}
        }
    }
}
