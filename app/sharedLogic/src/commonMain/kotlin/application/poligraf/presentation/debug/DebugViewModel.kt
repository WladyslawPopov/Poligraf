package application.poligraf.presentation.debug

import androidx.compose.runtime.Stable
import application.poligraf.presentation.base.BaseViewModel
import application.poligraf.engine.error.ErrorType
import application.poligraf.presentation.debug.data.DebugState
import application.poligraf.presentation.debug.data.DebugTab
import application.poligraf.uicore.theme.tokens.ColorToken
import application.poligraf.uicore.theme.tokens.TypographyToken
import application.poligraf.uicore.actions.DebugAction
import application.poligraf.uicore.actions.NavigationAction
import application.poligraf.uicore.actions.WidgetAction
import application.poligraf.uicore.types.BackgroundMode
import application.poligraf.uicore.types.ToastType
import application.poligraf.uicore.widgets.AppBackground
import application.poligraf.uicore.widgets.UiWidget
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
            baseColor = ColorToken.BACKGROUND,
            energyColor = ColorToken.ACCENT_ENERGY,
            particleColor = ColorToken.SURFACE_VARIANT,
            parallaxIntensity = 1.0f,
            blurRadius = 6.0f
        )
    ))
    val state: StateFlow<DebugState> = _state.asStateFlow()

    init {
        // Sync background mode with application states
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
                textProvider = { it.common.welcomeText },
                emoji = " 🛠️",
                colorToken = ColorToken.ACCENT_PRIMARY,
                typographyToken = TypographyToken.HEADER
            ),
            UiWidget.SubjectSlider(
                id = "debug_slider_1",
                items = listOf(
                    UiWidget.SubjectCard(
                        id = "mock_1",
                        titleProvider = { it.subjects.newTitle },
                        emoji = "🧪",
                        action = DebugAction.TriggerSuccessToast,
                        backgroundColor = ColorToken.GLASS_BASE,
                        buttonColor = ColorToken.TRUTH
                    ),
                    UiWidget.SubjectCard(
                        id = "mock_2",
                        titleProvider = { it.subjects.newTitle },
                        emoji = "💥",
                        action = DebugAction.TriggerErrorNonBlocking,
                        backgroundColor = ColorToken.GLASS_BASE,
                        buttonColor = ColorToken.STRESS
                    ),
                    UiWidget.SubjectCard(
                        id = "mock_3",
                        titleProvider = { it.subjects.newTitle },
                        emoji = "⏳",
                        action = DebugAction.TriggerLoading,
                        backgroundColor = ColorToken.GLASS_BASE,
                        buttonColor = ColorToken.ACCENT_ENERGY
                    )
                )
            ),
            UiWidget.WelcomeText(
                id = "debug_info",
                textProvider = { it.debug.title },
                colorToken = ColorToken.TEXT_SECONDARY,
                typographyToken = TypographyToken.SUBHEADER,
                typingDelay = 10L
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
                showToast({ it.errors.unknownTitle }, ToastType.ERROR)
            }
            DebugAction.TriggerSuccessToast -> {
                showToast({ it.toast.authSuccess }, ToastType.SUCCESS)
            }
            else -> {}
        }
    }
}
