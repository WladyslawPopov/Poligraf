package application.liedetector.presentation.recording

import application.liedetector.data.subject.SubjectRepository
import application.liedetector.engine.utils.nowAsEpochMilliseconds
import application.liedetector.engine.io.audio.AudioRecorder
import application.liedetector.models.KmpResult
import application.liedetector.navigation.AppNavigation
import application.liedetector.presentation.base.BaseViewModel
import application.liedetector.presentation.recording.data.RecordingState
import application.liedetector.uicore.theme.tokens.StringToken
import application.liedetector.uicore.types.BackgroundMode
import application.liedetector.uicore.types.ToastType
import application.liedetector.uicore.widgets.AppBackground
import application.liedetector.uicore.widgets.UiWidget
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
class RecordingViewModel(
    private val subjectId: String,
    private val navigation: AppNavigation,
    private val subjectRepository: SubjectRepository,
    private val audioRecorder: AudioRecorder
) : BaseViewModel() {

    private val _state = MutableStateFlow(RecordingState())
    val state: StateFlow<RecordingState> = _state.asStateFlow()

    init {
        // Sync background mode with application states
        combine(isLoading, errorType, toastState, audioRecorder.isRecording) { loading, error, toast, recording ->
            val currentBg = _state.value.background
            if (currentBg is AppBackground.AnimatedScales) {
                val newMode = when {
                    error != null -> BackgroundMode.ERROR
                    toast != null -> {
                        if (toast.type == ToastType.SUCCESS) BackgroundMode.SUCCESS else BackgroundMode.ERROR
                    }
                    recording -> BackgroundMode.RECORDING
                    loading -> BackgroundMode.PROCESSING
                    _state.value.activeRecorder == null -> BackgroundMode.WAITING
                    else -> BackgroundMode.IDLE
                }
                
                if (currentBg.mode != newMode) {
                    _state.value = _state.value.copy(
                        background = currentBg.copy(mode = newMode)
                    )
                }
            }
        }.launchIn(scope)

        // Sync recorder state with UI widget
        combine(
            audioRecorder.isRecording,
            audioRecorder.isPaused,
            audioRecorder.durationMillis,
            audioRecorder.amplitudes
        ) { recording, paused, duration, amplitudes ->
            if (recording || duration > 0) {
                updateRecorderWidget(recording, paused, duration, amplitudes)
            }
        }
        .sample(32.milliseconds) // Limit UI updates to ~30fps to avoid excessive recompositions
        .launchIn(scope)

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

    fun onMicClicked() {
        if (audioRecorder.isRecording.value) {
            // Already recording, maybe toggle pause? Or just ignore
        } else {
            startRecording()
        }
    }

    fun toggleRecording() {
        if (audioRecorder.isPaused.value) {
            audioRecorder.resume()
        } else {
            audioRecorder.pause()
        }
    }

    fun stopRecording() {
        val path = audioRecorder.stop()
        val active = _state.value.activeRecorder
        if (active != null) {
            _state.value = _state.value.copy(
                activeRecorder = null,
                widgets = _state.value.widgets.filter { it !is UiWidget.WelcomeText } + 
                        active.copy(status = UiWidget.VoiceRecorder.Status.FINISHED, filePath = path)
            )
        }
    }

    private fun startRecording() {
        audioRecorder.start()
        _state.value = _state.value.copy(
            activeRecorder = UiWidget.VoiceRecorder(id = "recorder_${nowAsEpochMilliseconds()}")
        )
    }

    private fun updateRecorderWidget(recording: Boolean, paused: Boolean, duration: Long, amplitudes: List<Float>) {
        val status = when {
            paused -> UiWidget.VoiceRecorder.Status.PAUSED
            recording -> UiWidget.VoiceRecorder.Status.RECORDING
            else -> UiWidget.VoiceRecorder.Status.FINISHED
        }

        _state.value = _state.value.copy(
            activeRecorder = _state.value.activeRecorder?.copy(
                status = status,
                durationMillis = duration,
                amplitudes = amplitudes
            )
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
