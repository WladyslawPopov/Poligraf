package application.liedetector.presentation.debug

import application.liedetector.domain.model.ErrorType
import application.liedetector.navigation.AppNavigation
import application.liedetector.presentation.base.BaseViewModel
import application.liedetector.presentation.debug.data.DebugState
import application.liedetector.presentation.debug.data.DebugTab
import application.liedetector.uicore.theme.tokens.ColorToken
import application.liedetector.uicore.theme.tokens.StringToken
import application.liedetector.uicore.theme.tokens.TypographyToken
import application.liedetector.uicore.types.ToastType
import application.liedetector.uicore.types.WidgetAction
import application.liedetector.uicore.widgets.AppBackground
import application.liedetector.uicore.widgets.BackgroundMode
import application.liedetector.uicore.widgets.UiWidget
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class DebugViewModel(private val navigation: AppNavigation) : BaseViewModel() {
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
            val currentBg = _state.value.background
            if (currentBg is AppBackground.AnimatedScales) {
                val newMode = when {
                    error != null -> BackgroundMode.ERROR
                    toast != null -> {
                        if (toast.type == ToastType.SUCCESS) BackgroundMode.SUCCESS else BackgroundMode.ERROR
                    }
                    loading -> BackgroundMode.PROCESSING
                    else -> BackgroundMode.IDLE
                }
                
                _state.value = _state.value.copy(
                    background = currentBg.copy(mode = newMode)
                )
            }
        }.launchIn(scope)

        _state.value = _state.value.copy(
            widgets = createMockWidgets()
        )
    }

    private fun createMockWidgets(): List<UiWidget> {
        return listOf(
            UiWidget.WelcomeText(
                id = "debug_welcome",
                textToken = StringToken.WELCOME_TEXT,
                emoji = " 🛠️",
                colorToken = ColorToken.ACCENT_PRIMARY,
                typographyToken = TypographyToken.HEADER
            ),
            UiWidget.SubjectSlider(
                id = "debug_slider_1",
                items = listOf(
                    UiWidget.SubjectCard(
                        id = "mock_1",
                        titleToken = StringToken.SUBJECT_NEW_TITLE,
                        emoji = "🧪",
                        action = WidgetAction.DEBUG_TRIGGER_SUCCESS_TOAST,
                        backgroundColor = ColorToken.GLASS_BASE,
                        buttonColor = ColorToken.TRUTH
                    ),
                    UiWidget.SubjectCard(
                        id = "mock_2",
                        titleToken = StringToken.SUBJECT_NEW_TITLE,
                        emoji = "💥",
                        action = WidgetAction.DEBUG_TRIGGER_ERROR_NON_BLOCKING,
                        backgroundColor = ColorToken.GLASS_BASE,
                        buttonColor = ColorToken.STRESS
                    ),
                    UiWidget.SubjectCard(
                        id = "mock_3",
                        titleToken = StringToken.SUBJECT_NEW_TITLE,
                        emoji = "⏳",
                        action = WidgetAction.DEBUG_TRIGGER_LOADING,
                        backgroundColor = ColorToken.GLASS_BASE,
                        buttonColor = ColorToken.ACCENT_ENERGY
                    )
                )
            ),
            UiWidget.WelcomeText(
                id = "debug_info",
                textToken = StringToken.DEBUG_TITLE,
                colorToken = ColorToken.TEXT_SECONDARY,
                typographyToken = TypographyToken.SUBHEADER,
                typingDelay = 10L
            )
        )
    }

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
