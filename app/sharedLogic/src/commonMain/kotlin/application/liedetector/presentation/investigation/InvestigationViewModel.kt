package application.liedetector.presentation.investigation

import application.liedetector.data.subject.SubjectRepository
import application.liedetector.models.KmpResult
import application.liedetector.navigation.AppNavigation
import application.liedetector.presentation.base.BaseViewModel
import application.liedetector.presentation.investigation.data.InvestigationState
import application.liedetector.uicore.theme.tokens.StringToken
import application.liedetector.uicore.types.BackgroundMode
import application.liedetector.uicore.types.ToastType
import application.liedetector.uicore.widgets.AppBackground
import application.liedetector.uicore.widgets.UiWidget
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlin.time.Duration.Companion.milliseconds

class InvestigationViewModel(
    private val subjectId: String,
    private val navigation: AppNavigation,
    private val subjectRepository: SubjectRepository
) : BaseViewModel() {

    private val _state = MutableStateFlow(InvestigationState())
    val state: StateFlow<InvestigationState> = _state.asStateFlow()

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
                    // Start with WAITING, switch to RECORDING when actually recording
                    _state.value.widgets.isEmpty() -> BackgroundMode.WAITING
                    else -> BackgroundMode.RECORDING
                }
                
                if (currentBg.mode != newMode) {
                    _state.value = _state.value.copy(
                        background = currentBg.copy(mode = newMode)
                    )
                }
            }
        }.launchIn(scope)

        loadInvestigation()
    }

    private fun loadInvestigation() {
        launchSafe(
            block = {
                val result = subjectRepository.getSubject(subjectId)
                if (result is KmpResult.Success) {
                    _state.value = _state.value.copy(
                        subject = result.data,
                        widgets = if (_state.value.widgets.isEmpty()) {
                            listOf(
                                UiWidget.WelcomeText(
                                    id = "investigation_greeting",
                                    textToken = StringToken.INVESTIGATION_SCREEN_PLACEHOLDER,
                                    emoji = " 👋",
                                    typingDelay = 30L
                                )
                            )
                        } else _state.value.widgets
                    )
                }
            }
        )
    }

    fun deleteSubject() {
        launchSafe(
            block = {
                // TODO: Implement delete in repository/server
                showToast(StringToken.DEBUG_TRIGGER_SUCCESS_TOAST, ToastType.SUCCESS)
                delay(500.milliseconds)
                navigation.back()
            }
        )
    }

    fun goBack() {
        navigation.back()
    }
}
