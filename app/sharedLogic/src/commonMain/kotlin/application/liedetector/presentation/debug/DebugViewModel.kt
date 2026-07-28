package application.liedetector.presentation.debug

import application.liedetector.navigation.AppNavigation
import application.liedetector.presentation.base.BaseViewModel
import application.liedetector.presentation.debug.data.DebugState
import application.liedetector.presentation.debug.data.DebugTab
import application.liedetector.uicore.state.ErrorType
import application.liedetector.uicore.state.ToastType
import application.liedetector.uicore.theme.ColorToken
import application.liedetector.uicore.theme.StringToken
import application.liedetector.uicore.types.WidgetAction
import application.liedetector.uicore.widgets.UiWidget
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class DebugViewModel(private val navigation: AppNavigation) : BaseViewModel() {
    private val _state = MutableStateFlow(DebugState())
    val state: StateFlow<DebugState> = _state.asStateFlow()

    init {
        loadMockWidgets()
    }

    fun setTab(tab: DebugTab) {
        _state.value = _state.value.copy(selectedTab = tab)
    }

    fun goBack() {
        navigation.back()
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
                    delay(2000.milliseconds)
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
