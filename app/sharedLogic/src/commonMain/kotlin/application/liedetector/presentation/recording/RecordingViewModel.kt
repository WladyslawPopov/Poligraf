package application.liedetector.presentation.recording

import application.liedetector.data.subject.SubjectRepository
import application.liedetector.models.KmpResult
import application.liedetector.navigation.AppNavigation
import application.liedetector.presentation.base.BaseViewModel
import application.liedetector.presentation.recording.data.RecordingState
import application.liedetector.uicore.theme.tokens.StringToken
import application.liedetector.uicore.types.BackgroundMode
import application.liedetector.uicore.types.ToastType
import application.liedetector.uicore.widgets.AppBackground
import application.liedetector.uicore.widgets.UiWidget
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlin.time.Duration.Companion.milliseconds

class RecordingViewModel(
    private val subjectId: String,
    private val navigation: AppNavigation,
    private val subjectRepository: SubjectRepository
) : BaseViewModel() {

    private val _state = MutableStateFlow(RecordingState())
    val state: StateFlow<RecordingState> = _state.asStateFlow()

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

        loadRecording()
    }

    private fun loadRecording() {
        launchSafe(
            block = {
                val result = subjectRepository.getSubject(subjectId)
                if (result is KmpResult.Success) {
                    _state.value = _state.value.copy(
                        subject = result.data,
                        widgets = _state.value.widgets.ifEmpty {
                            listOf(
                                UiWidget.WelcomeText(
                                    id = "recording_greeting",
                                    textToken = StringToken.RECORDING_SCREEN_PLACEHOLDER,
                                    emoji = "🎙️",
                                    typingDelay = 30L
                                )
                            )
                        }
                    )
                }
            }
        )
    }

    fun deleteRecording() {
        launchSafe(
            isBlocking = true,
            block = {
                val result = subjectRepository.deleteSubjects(listOf(subjectId))
                if (result is KmpResult.Success) {
                    showToast(StringToken.DELETE_RECORDING_CONFIRMATION, ToastType.SUCCESS)
                    delay(500.milliseconds)
                    navigation.back()
                } else if (result is KmpResult.Error) {
                    // Error handled by BaseViewModel
                }
            }
        )
    }

    fun goBack() {
        navigation.back()
    }
}
