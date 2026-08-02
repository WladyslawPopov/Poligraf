package application.liedetector.presentation.main

import application.liedetector.data.user.UserRepository
import application.liedetector.models.KmpResult
import application.liedetector.navigation.AppNavigation
import application.liedetector.presentation.base.BaseViewModel
import application.liedetector.presentation.base.toErrorType
import application.liedetector.uicore.theme.tokens.ColorToken
import application.liedetector.uicore.theme.tokens.StringToken
import application.liedetector.uicore.theme.tokens.TypographyToken
import application.liedetector.uicore.types.ToastType
import application.liedetector.uicore.types.WidgetAction
import application.liedetector.uicore.widgets.AppBackground
import application.liedetector.uicore.widgets.BackgroundMode
import application.liedetector.uicore.widgets.UiWidget
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainViewModel(
    private val userRepository: UserRepository,
    private val navigation: AppNavigation
) : BaseViewModel()
{
    private val _state = MutableStateFlow(MainState(
        background = AppBackground.AnimatedScales(
            baseColor = ColorToken.BACKGROUND,
            energyColor = ColorToken.ACCENT_ENERGY,
            particleColor = ColorToken.SURFACE_VARIANT,
            parallaxIntensity = 1.2f,
            blurRadius = 4.0f
        ),
        toolbar = UiWidget.AppToolbar(
            id = "main_toolbar",
            titleToken = null,
            backgroundColor = ColorToken.BACKGROUND,
            contentColor = ColorToken.TEXT_PRIMARY
        ),
        welcomeWidget = UiWidget.WelcomeText(
            id = "main_welcome",
            textToken = StringToken.WELCOME_TEXT,
            colorToken = ColorToken.TEXT_PRIMARY,
            typographyToken = TypographyToken.HEADER
        ),
        widgets = listOf(createDefaultSlider(emptyList()))
    ))
    val state: StateFlow<MainState> = _state.asStateFlow()

    init {
        // Observe display metrics and adjust widgets
        displayMetrics
            .onEach { metrics ->
                updateAdaptiveContent(metrics.isLandscape)
            }
            .launchIn(scope)

        // Sync background mode with application states
        combine(isLoading, errorType, toastState) { _, error, toast ->
            val currentBg = _state.value.background
            if (currentBg is AppBackground.AnimatedScales) {
                val newMode = when {
                    error != null -> BackgroundMode.ERROR
                    toast != null -> {
                        if (toast.type == ToastType.SUCCESS) BackgroundMode.SUCCESS else BackgroundMode.ERROR
                    }
                    // No PROCESSING mode for Main screen to avoid quick flickering
                    else -> BackgroundMode.IDLE
                }
                
                if (currentBg.mode != newMode) {
                    _state.value = _state.value.copy(
                        background = currentBg.copy(mode = newMode)
                    )
                }
            }
        }.launchIn(scope)

        loadContent()
    }

    private fun createDefaultSlider(serverItems: List<UiWidget.SubjectCard>): UiWidget.SubjectSlider {
        val defaultCard = UiWidget.SubjectCard(
            id = "new_investigation",
            titleToken = StringToken.SUBJECT_NEW_TITLE,
            emoji = "🕵️",
            action = WidgetAction.START_NEW_INVESTIGATION,
            backgroundColor = ColorToken.GLASS_BASE,
            titleColor = ColorToken.TEXT_PRIMARY,
            titleTypography = TypographyToken.SUBHEADER,
            buttonColor = ColorToken.ACCENT_PRIMARY
        )
        return UiWidget.SubjectSlider(
            id = "main_slider",
            itemSpacing = 16,
            items = listOf(defaultCard) + serverItems
        )
    }

    private fun updateAdaptiveContent(isLandscape: Boolean) {
        val welcome = _state.value.welcomeWidget ?: return
        // Example of adjusting typing delay or other properties based on orientation
        _state.value = _state.value.copy(
            welcomeWidget = welcome.copy(
                typingDelay = if (isLandscape) 20L else 40L 
            )
        )
    }
    
    fun loadContent() {
        _state.value = _state.value.copy(errorRaw = null, errorToken = null)
        
        launchSafe(
            block = {
                val result = userRepository.getMainScreen()
                if (result is KmpResult.Success) {
                    Napier.d { "MAIN: Content loaded successfully from SERVER" }
                    // Filter out any SubjectSliders from server to replace them with our merged one
                    val otherWidgets = result.data.filter { it !is UiWidget.SubjectSlider }
                    val serverCards = result.data
                        .filterIsInstance<UiWidget.SubjectSlider>()
                        .flatMap { it.items }

                    _state.value = _state.value.copy(
                        widgets = listOf(createDefaultSlider(serverCards)) + otherWidgets,
                        errorRaw = null, 
                        errorToken = null
                    )
                } else if (result is KmpResult.Error) {
                    Napier.e(result.throwable) { "MAIN: Server Error - ${result.throwable.message}" }
                    setManualError(result.throwable.toErrorType())
                }
            }
        )
    }

    fun onWidgetAction(action: WidgetAction) {
        Napier.d { "Action triggered: $action" }
        when (action) {
            WidgetAction.OPEN_HISTORY -> {
                navigation.openMain()
            }
            WidgetAction.OPEN_SETTINGS -> {
                navigation.toggleDrawer()
            }
            WidgetAction.OPEN_PROFILE -> {
                Napier.d { "MAIN: Profile action triggered" }
            }
            WidgetAction.START_NEW_INVESTIGATION -> {
                Napier.d { "MAIN: Start New Investigation triggered" }
            }
            else -> {}
        }
    }
}
