package application.poligraf.presentation.main

import androidx.compose.runtime.Stable
import application.poligraf.presentation.base.BaseViewModel
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.StringToken
import application.poligraf.ui.foundation.actions.NavigationAction
import application.poligraf.ui.foundation.actions.WidgetAction
import application.poligraf.ui.foundation.models.AppBackground
import application.poligraf.engine.config.AppConfig
import application.poligraf.engine.settings.SettingsRepository
import application.poligraf.domain.repository.AnalyzerRepository
import application.poligraf.presentation.main.data.MainBottomSheetContent
import application.poligraf.presentation.main.data.MainState
import application.poligraf.ui.foundation.actions.RecordingAction
import application.poligraf.ui.foundation.models.AppToolbar
import application.poligraf.ui.foundation.models.UiWidget
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

import kotlinx.coroutines.launch

@Stable
class MainViewModel(
    private val appConfig: AppConfig,
    private val analyzerRepository: AnalyzerRepository,
    private val navigateToDebug: () -> Unit
) : BaseViewModel()
{
    init {
        scope.launch {
            val draft = analyzerRepository.getActiveDraft()
            if (draft != null) {
                analyzerRepository.resumeFromDraft(draft.first, draft.second)
                openAnalyzer(true)
            }
        }
    }

    private val welcomeData = listOf(
        StringToken.WELCOME_2 to "📊",
        StringToken.WELCOME_3 to "📡",
        StringToken.WELCOME_4 to "🔴"
    ).random()

    private val _state = MutableStateFlow(
        MainState(
            background = AppBackground.AnimatedScales(
                baseColor = ColorToken.SURFACE_BACKGROUND,
                energyColor = ColorToken.ACCENT_ENERGY,
                particleColor = ColorToken.SURFACE_VARIANT,
                blurRadius = 4.0f
            ),
            toolbar = AppToolbar(
                titleToken = StringToken.WELCOME_1,
                historyAction = NavigationAction.History,
                settingsAction = NavigationAction.Settings,
                backgroundColor = ColorToken.SURFACE_BACKGROUND,
                contentColor = ColorToken.TEXT_PRIMARY
            ),
            appConfig = appConfig,
            widgets = listOf(
                UiWidget.WelcomeText(
                    id = "main_welcome",
                    textToken = welcomeData.first,
                    emoji = welcomeData.second,
                    colorToken = ColorToken.TEXT_PRIMARY
                ),
                UiWidget.AnalyzeBtn(
                    id = "analyze_btn",
                    action = RecordingAction.StartNew
                )
            )
        )
    )
    val state: StateFlow<MainState> = _state.asStateFlow()

    fun openSettings(isOpen: Boolean) {
        _state.update {
            if (isOpen){
                it.copy(
                    bottomSheetState = true,
                    bottomSheetContent = MainBottomSheetContent.SETTINGS
                )
            } else {
                it.copy(
                    bottomSheetState = false,
                    bottomSheetContent = MainBottomSheetContent.NONE
                )
            }
        }
    }

    fun openAnalyzer(isOpen: Boolean) {
        _state.update {
            if (isOpen){
                it.copy(
                    bottomSheetState = true,
                    bottomSheetContent = MainBottomSheetContent.ANALYZER
                )
            } else {
                it.copy(
                    bottomSheetState = false,
                    bottomSheetContent = MainBottomSheetContent.NONE
                )
            }
        }
    }

    fun onWidgetAction(action: WidgetAction) {
        Napier.d { "Action triggered: $action" }
        when (action) {
            is NavigationAction.History -> {}
            is NavigationAction.Settings -> openSettings(!_state.value.bottomSheetState)
            is RecordingAction.StartNew -> {
                openAnalyzer(!_state.value.bottomSheetState)
            }
            else -> {}
        }
    }

    fun onDebugClicked() {
        navigateToDebug()
    }

    fun closeBottomSheet() {
        _state.update {
            it.copy(
                bottomSheetState = false,
                bottomSheetContent = MainBottomSheetContent.NONE
            )
        }
    }

    fun loadContent() {
    }
}
