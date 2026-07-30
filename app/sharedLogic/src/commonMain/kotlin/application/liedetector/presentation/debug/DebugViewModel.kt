package application.liedetector.presentation.debug

import application.liedetector.navigation.AppNavigation
import application.liedetector.presentation.base.BaseViewModel
import application.liedetector.presentation.debug.data.DebugState
import application.liedetector.presentation.debug.data.DebugTab
import application.liedetector.uicore.theme.tokens.StringToken
import application.liedetector.uicore.types.ErrorType
import application.liedetector.uicore.types.ToastType
import application.liedetector.uicore.types.WidgetAction
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class DebugViewModel(private val navigation: AppNavigation) : BaseViewModel() {
    private val _state = MutableStateFlow(DebugState())
    val state: StateFlow<DebugState> = _state.asStateFlow()

    fun setTab(tab: DebugTab) {
        _state.value = _state.value.copy(selectedTab = tab)
    }

    fun goBack() {
        navigation.back()
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
