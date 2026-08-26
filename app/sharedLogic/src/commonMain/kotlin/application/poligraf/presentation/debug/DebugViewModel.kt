package application.poligraf.presentation.debug

import androidx.compose.runtime.Stable
import application.poligraf.presentation.base.BaseViewModel
import application.poligraf.engine.error.ErrorType
import application.poligraf.presentation.debug.data.DebugState
import application.poligraf.presentation.debug.data.DebugTab
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.StringToken
import application.poligraf.ui.foundation.actions.DebugAction
import application.poligraf.ui.foundation.actions.NavigationAction
import application.poligraf.ui.foundation.actions.WidgetAction
import application.poligraf.ui.foundation.types.BackgroundMode
import application.poligraf.ui.foundation.types.ToastType
import application.poligraf.ui.foundation.models.AppBackground
import application.poligraf.ui.foundation.models.UiWidget
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Stable
class DebugViewModel(
    private val navigateBack: () -> Unit,
    private val navigateToMain: () -> Unit
) : BaseViewModel() {
    private val _state = MutableStateFlow(DebugState(
        background = AppBackground.AnimatedScales(
            baseColor = ColorToken.SURFACE_BACKGROUND,
            energyColor = ColorToken.ACCENT_ENERGY,
            particleColor = ColorToken.SURFACE_VARIANT,
            blurRadius = 6.0f
        )
    ))
    val state: StateFlow<DebugState> = _state.asStateFlow()

    private val welcomeData = listOf(
        StringToken.WELCOME_1 to "🛠️",
        StringToken.WELCOME_2 to "📊",
        StringToken.WELCOME_3 to "📡",
        StringToken.WELCOME_4 to "🔴"
    ).random()

    init {
        combine(isLoading, errorType, toastState) { loading, error, toast ->
            _state.update { currentState ->
                val currentBg = currentState.background
                if (currentBg is AppBackground.AnimatedScales) {
                    val newMode = when {
                        error != null -> BackgroundMode.ERROR
                        toast != null -> {
                            if (toast.type == ToastType.SUCCESS) BackgroundMode.SUCCESS else BackgroundMode.ERROR
                        }
                        loading -> BackgroundMode.PROCESSING
                        else -> BackgroundMode.IDLE
                    }
                    
                    if (currentBg.mode != newMode) {
                        currentState.copy(
                            background = currentBg.copy(mode = newMode)
                        )
                    } else {
                        currentState
                    }
                } else {
                    currentState
                }
            }
        }.launchIn(scope)

        _state.update { 
            it.copy(widgets = createMockWidgets())
        }
    }

    private fun createMockWidgets(): List<UiWidget> {
        return listOf(
            UiWidget.WelcomeText(
                id = "debug_welcome",
                textToken = welcomeData.first,
                emoji = welcomeData.second,
                colorToken = ColorToken.ACCENT_PRIMARY
            )
        )
    }

    fun setTab(tab: DebugTab) {
        _state.update { it.copy(selectedTab = tab) }
    }

    fun goBack() {
        navigateBack()
    }

    fun onWidgetAction(action: WidgetAction) {
        when (action) {
            NavigationAction.History -> {
                navigateToMain()
            }
            DebugAction.TriggerLoading -> {
                scope.launch {
                    setLoading(true)
                    delay(2000.milliseconds)
                    setLoading(false)
                }
            }
            DebugAction.TriggerErrorBlocking -> {
                setManualError(ErrorType.SERVER_UNAVAILABLE)
            }
            DebugAction.TriggerErrorNonBlocking -> {
                showToast({ it.errors.message }, ToastType.ERROR)
            }
            DebugAction.TriggerSuccessToast -> {
                showToast({ it.debug.triggerSuccess }, ToastType.SUCCESS)
            }
            else -> {}
        }
    }
}
