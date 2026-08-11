package application.liedetector.presentation.recording

import application.liedetector.data.base.BaseViewModel
import application.liedetector.data.subject.SubjectRepository
import application.liedetector.models.KmpResult
import application.liedetector.engine.navigation.AppNavigation
import application.liedetector.presentation.recording.data.MaterialTag
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

import application.liedetector.uicore.theme.tokens.ColorToken
import application.liedetector.uicore.theme.tokens.IconToken
import application.liedetector.uicore.widgets.VoiceRecorder
import application.liedetector.presentation.recordingHistory.*

import application.liedetector.engine.utils.convertHoursAndMinutes
import application.liedetector.engine.utils.nowAsEpochSeconds

@OptIn(FlowPreview::class)
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

        loadRecording()
    }

    private fun loadRecording() {
        launchSafe(
            block = {
                val result = subjectRepository.getSubject(subjectId)
                if (result is KmpResult.Success) {
                    _state.update { 
                        it.copy(
                            subject = result.data,
                            materials = listOf(
                                MaterialTag("recordings", "Recordings", "🎙️"),
                                MaterialTag("documents", "Docs", "📄"),
                                MaterialTag("photos", "Gallery", "🖼️")
                            ),
                            widgets = it.widgets.ifEmpty {
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
            }
        )
    }

    fun onMicClicked() {
        navigation.openRecordingsHistory(subjectId, startRecording = true)
    }

    fun onGalleryClicked() {
        // TODO: Handle subjects/$subjectId/photos
    }

    fun onNoteClicked() {
        // TODO: Handle subjects/$subjectId/notes or documents
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
                }
            }
        )
    }

    fun onMaterialTagClicked(tagId: String) {
        when (tagId) {
            "recordings" -> {
                navigation.openRecordingsHistory(subjectId, startRecording = false)
            }
            "documents" -> {
                // TODO: Open Documents screen or folder subjects/$subjectId/documents
            }
            "photos" -> {
                // TODO: Open Gallery screen or folder subjects/$subjectId/photos
            }
        }
    }

    fun goBack() {
        navigation.back()
    }
}
