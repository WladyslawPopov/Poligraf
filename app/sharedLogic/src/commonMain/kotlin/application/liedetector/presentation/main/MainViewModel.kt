package application.liedetector.presentation.main

import application.liedetector.domain.model.Subject
import application.liedetector.data.subject.SubjectRepository
import application.liedetector.models.KmpResult
import application.liedetector.navigation.AppNavigation
import application.liedetector.presentation.base.BaseViewModel
import application.liedetector.presentation.base.toErrorType
import application.liedetector.uicore.theme.tokens.ColorToken
import application.liedetector.uicore.theme.tokens.StringToken
import application.liedetector.uicore.theme.tokens.TypographyToken
import application.liedetector.uicore.actions.RecordingAction
import application.liedetector.uicore.actions.NavigationAction
import application.liedetector.uicore.actions.WidgetAction
import application.liedetector.uicore.types.BackgroundMode
import application.liedetector.uicore.types.ToastType
import application.liedetector.uicore.widgets.AppBackground
import application.liedetector.uicore.widgets.UiWidget
import application.liedetector.engine.config.AppConfig
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.random.Random

class MainViewModel(
    private val subjectRepository: SubjectRepository,
    private val appConfig: AppConfig,
    private val navigation: AppNavigation
) : BaseViewModel()
{
    private val defaultEmojis = listOf("🕵️", "👤", "👥", "❓", "👀", "🧠", "💼", "🔐")
    private val _state = MutableStateFlow(MainState(
        appConfig = appConfig,
        background = AppBackground.AnimatedScales(
            baseColor = ColorToken.BACKGROUND,
            energyColor = ColorToken.ACCENT_ENERGY,
            particleColor = ColorToken.SURFACE_VARIANT,
            parallaxIntensity = 1.2f,
            blurRadius = 4.0f
        ),
        toolbar = UiWidget.AppToolbar(
            id = "main_toolbar",
            titleToken = StringToken.APP_NAME,
            backgroundColor = ColorToken.BACKGROUND,
            contentColor = ColorToken.TEXT_PRIMARY
        ),
        welcomeWidget = UiWidget.WelcomeText(
            id = "main_welcome",
            textToken = StringToken.WELCOME_TEXT,
            colorToken = ColorToken.TEXT_PRIMARY,
            typographyToken = TypographyToken.HEADER
        ),
        widgets = listOf(MainWidgetFactory.createSubjectSlider(UiWidget.SubjectSlider.DisplayMode.FULL))
    ))
    val state: StateFlow<MainState> = _state.asStateFlow()

    init {
        // Observe display metrics and adjust widgets
        displayMetrics
            .onEach { metrics ->
                updateAdaptiveContent(metrics.isLandscape)
            }
            .launchIn(scope)

        // Observe subjects from cache/DB
        subjectRepository.getSubjects()
            .onEach { subjects ->
                updateStateWithSubjects(subjects)
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

    private fun updateAdaptiveContent(isLandscape: Boolean) {
        val welcome = _state.value.welcomeWidget ?: return
        // Example of adjusting typing delay or other properties based on orientation
        _state.value = _state.value.copy(
            welcomeWidget = welcome.copy(
                typingDelay = if (isLandscape) 20L else 40L 
            )
        )
    }
    
    private fun updateStateWithSubjects(subjects: List<Subject>) {
        Napier.d { "MAIN: Received ${subjects.size} subjects from stream" }
        if (subjects.isEmpty()) {
            _state.value = _state.value.copy(
                toolbar = UiWidget.AppToolbar(
                    id = "main_toolbar",
                    titleToken = StringToken.APP_NAME,
                    backgroundColor = ColorToken.BACKGROUND,
                    contentColor = ColorToken.TEXT_PRIMARY
                ),
                welcomeWidget = UiWidget.WelcomeText(
                    id = "main_welcome",
                    textToken = StringToken.WELCOME_TEXT,
                    colorToken = ColorToken.TEXT_PRIMARY,
                    typographyToken = TypographyToken.HEADER
                ),
                widgets = listOf(
                    MainWidgetFactory.createSubjectSlider(
                        UiWidget.SubjectSlider.DisplayMode.FULL
                    )
                ),
                errorRaw = null,
                errorToken = null
            )
        } else {
            _state.value = _state.value.copy(
                toolbar = UiWidget.AppToolbar(
                    id = "main_toolbar",
                    titleToken = StringToken.WELCOME_TEXT,
                    backgroundColor = ColorToken.BACKGROUND,
                    contentColor = ColorToken.TEXT_PRIMARY
                ),
                welcomeWidget = null,
                widgets = listOf(
                    MainWidgetFactory.createSubjectSlider(
                        UiWidget.SubjectSlider.DisplayMode.RECT_STORY
                    ),
                    MainWidgetFactory.createSubjectList(subjects)
                ),
                errorRaw = null,
                errorToken = null
            )
        }
    }

    fun loadContent() {
        _state.value = _state.value.copy(errorRaw = null, errorToken = null)
        
        launchSafe(
            isBlocking = false,
            block = {
                val result = subjectRepository.syncSubjects()
                if (result is KmpResult.Error) {
                    setManualError(result.throwable.toErrorType())
                }
            }
        )
    }

    fun onWidgetAction(action: WidgetAction) {
        Napier.d { "Action triggered: $action" }
        when (action) {
            is WidgetAction.ToggleSelection -> toggleSelection(action.id)
            is WidgetAction.DeleteSelected -> deleteSelected()
            is WidgetAction.ClearSelection -> clearSelection()
            is NavigationAction.History -> {
                navigation.openMain()
            }
            is NavigationAction.Settings -> {
                navigation.toggleDrawer()
            }
            is NavigationAction.Profile -> {
                Napier.d { "MAIN: Profile action triggered" }
            }
            is RecordingAction.StartNew -> {
                startNewRecording()
            }
            is RecordingAction.Open -> {
                val currentList = _state.value.widgets.filterIsInstance<UiWidget.SubjectList>().firstOrNull()
                if (currentList?.isSelectionMode == true) {
                    toggleSelection(action.subjectId)
                } else {
                    navigation.openRecording(action.subjectId)
                }
            }
            else -> {}
        }
    }

    private fun toggleSelection(id: String) {
        val currentWidgets = _state.value.widgets.toMutableList()
        val listIndex = currentWidgets.indexOfFirst { it is UiWidget.SubjectList }
        if (listIndex != -1) {
            val list = currentWidgets[listIndex] as UiWidget.SubjectList
            val newSelected = list.selectedIds.toMutableSet()
            if (newSelected.contains(id)) newSelected.remove(id) else newSelected.add(id)
            
            val isSelectionMode = newSelected.isNotEmpty()
            currentWidgets[listIndex] = list.copy(
                isSelectionMode = isSelectionMode,
                selectedIds = newSelected
            )
            
            _state.value = _state.value.copy(
                widgets = currentWidgets
            )
        }
    }

    private fun clearSelection() {
        val currentWidgets = _state.value.widgets.toMutableList()
        val listIndex = currentWidgets.indexOfFirst { it is UiWidget.SubjectList }
        if (listIndex != -1) {
            val list = currentWidgets[listIndex] as UiWidget.SubjectList
            currentWidgets[listIndex] = list.copy(
                isSelectionMode = false,
                selectedIds = emptySet()
            )
            _state.value = _state.value.copy(
                widgets = currentWidgets
            )
        }
    }

    private fun deleteSelected() {
        val currentList = _state.value.widgets.filterIsInstance<UiWidget.SubjectList>().firstOrNull() ?: return
        val idsToDelete = currentList.selectedIds
        
        if (idsToDelete.isEmpty()) return

        launchSafe(
            isBlocking = true,
            block = {
                Napier.d { "Deleting subjects: $idsToDelete" }
                val result = subjectRepository.deleteSubjects(idsToDelete.toList())
                if (result is KmpResult.Success) {
                    clearSelection()
                    loadContent()
                } else if (result is KmpResult.Error) {
                    setManualError(result.throwable.toErrorType())
                }
            }
        )
    }

    private fun startNewRecording() {
        launchSafe(
            isBlocking = true,
            block = {
                val emoji = defaultEmojis[Random.nextInt(defaultEmojis.size)]
                val result = subjectRepository.createSubject(
                    name = "Undefined-1",
                    avatar = emoji,
                    isDefaultAvatar = true,
                    description = "Initial automated subject"
                )
                
                if (result is KmpResult.Success) {
                    val subject = result.data
                    navigation.openRecording(subject.id ?: "")
                }
            }
        )
    }
}
