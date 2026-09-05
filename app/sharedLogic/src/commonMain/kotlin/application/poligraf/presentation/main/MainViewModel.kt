package application.poligraf.presentation.main

import androidx.compose.runtime.Stable
import application.poligraf.domain.analyzer.repository.AnalyzerRepository
import application.poligraf.domain.analyzer.types.AnalyzerSkin
import application.poligraf.domain.analyzer.types.MarkerShape
import application.poligraf.domain.analyzer.types.QuantumWindowDuration
import application.poligraf.domain.analyzer.types.SensitivityLevel
import application.poligraf.domain.preferences.repository.PreferencesRepository
import application.poligraf.engine.config.AppConfig
import application.poligraf.engine.device.AppPermission
import application.poligraf.engine.device.PermissionManager
import application.poligraf.presentation.base.BaseViewModel
import application.poligraf.presentation.main.data.MainState
import application.poligraf.ui.features.analyzer.actions.AnalyzingAction
import application.poligraf.ui.features.main.models.MainAnalyzeBtnModel
import application.poligraf.ui.features.main.models.MainWelcomeModel
import application.poligraf.ui.features.main.models.NavigationAction
import application.poligraf.ui.foundation.models.AppBackground
import application.poligraf.ui.foundation.models.AppToolbar
import application.poligraf.ui.foundation.models.ToolbarAction
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.IconToken
import application.poligraf.ui.theme.tokens.StringToken
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
    private val permissionManager: PermissionManager,
    private val preferencesRepository: PreferencesRepository,
    private val navigateToDebug: () -> Unit,
    private val navigateToHistory: () -> Unit,
    private val navigateToAnalyzer: () -> Unit,
) : BaseViewModel() {

    val defaultSkin = preferencesRepository.skinFlow
    val markerShape = preferencesRepository.markerShapeFlow
    val sensitivity = preferencesRepository.sensitivityFlow
    val quantumWindow = preferencesRepository.quantumWindowFlow

    fun onSkinSelected(skin: AnalyzerSkin) = preferencesRepository.setSkin(skin)
    fun onMarkerShapeSelected(shape: MarkerShape) = preferencesRepository.setMarkerShape(shape)
    fun onSensitivitySelected(level: SensitivityLevel) = preferencesRepository.setSensitivity(level)
    fun onQuantumWindowSelected(duration: QuantumWindowDuration) = preferencesRepository.setQuantumWindow(duration)

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
                navigationIcon = IconToken.SETTINGS,
                navigationAction = NavigationAction.Settings,
                trailingActions = listOf(
                    ToolbarAction(
                        icon = IconToken.HISTORY,
                        action = NavigationAction.History
                    )
                ),
                backgroundColor = ColorToken.SURFACE_BACKGROUND,
                contentColor = ColorToken.TEXT_PRIMARY
            ),
            appConfig = appConfig,
            welcomeWidget = MainWelcomeModel(
                textToken = welcomeData.first,
                emoji = welcomeData.second,
                colorToken = ColorToken.TEXT_PRIMARY
            ),
            analyzeBtn = MainAnalyzeBtnModel()
        )
    )
    val state: StateFlow<MainState> = _state.asStateFlow()

    private var isRequestingPermission = false

    init {
        scope.launch {
            if (analyzerRepository.getActiveDraft() != null) {
                openAnalyzer(true)
            }
        }

        // Listen for permission changes
        scope.launch {
            permissionManager.permissionsState.collect { permissions ->
                if (permissions[AppPermission.RECORD_AUDIO] == true && isRequestingPermission) {
                    isRequestingPermission = false
                    openAnalyzer(true)
                }
            }
        }
    }

    fun openSettings(isOpen: Boolean) {
        _state.update {
            if (isOpen) {
                it.copy(
                    bottomSheetState = true
                )
            } else {
                it.copy(
                    bottomSheetState = false
                )
            }
        }
    }

    fun openAnalyzer(isOpen: Boolean) {
        if (isOpen) {
            navigateToAnalyzer()
        }
    }

    fun onAction(action: Any) {
        Napier.d { "Action triggered: $action" }
        when (action) {
            is NavigationAction.History -> navigateToHistory()
            is NavigationAction.Settings -> openSettings(!_state.value.bottomSheetState)
            is AnalyzingAction.StartNew -> onAnalyzeClick()
            else -> {}
        }
    }

    fun onAnalyzeClick() {
        if (permissionManager.isGranted(AppPermission.RECORD_AUDIO)) {
            openAnalyzer(true)
        } else {
            isRequestingPermission = true
            permissionManager.requestPermission(AppPermission.RECORD_AUDIO)
        }
    }

    fun onDebugClicked() {
        navigateToDebug()
    }

    fun closeBottomSheet() {
        _state.update {
            it.copy(
                bottomSheetState = false
            )
        }
    }
}
