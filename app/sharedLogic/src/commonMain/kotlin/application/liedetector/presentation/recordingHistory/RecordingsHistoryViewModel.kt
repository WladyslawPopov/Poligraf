package application.liedetector.presentation.recordingHistory

import application.liedetector.data.base.BaseViewModel
import application.liedetector.data.subject.SubjectRepository
import application.liedetector.engine.io.audio.AudioRecorder
import application.liedetector.models.KmpResult
import application.liedetector.engine.navigation.AppNavigation
import application.liedetector.engine.utils.nowAsEpochMilliseconds
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
class RecordingsHistoryViewModel(
    private val subjectId: String,
    private val navigation: AppNavigation,
    private val subjectRepository: SubjectRepository,
    private val audioRecorder: AudioRecorder,
    private val startRecording: Boolean = false
) : BaseViewModel() {

    private val _state = MutableStateFlow(RecordingState())
    val state: StateFlow<RecordingState> = _state.asStateFlow()

    init {
        // Sync background mode with application states
        combine(isLoading, errorType, toastState, audioRecorder.isRecording) { loading, error, toast, recording ->
            _state.update { currentState ->
                val currentBg = currentState.background
                if (currentBg is AppBackground.AnimatedScales) {
                    val newMode = when {
                        error != null -> BackgroundMode.ERROR
                        toast != null -> {
                            if (toast.type == ToastType.SUCCESS) BackgroundMode.SUCCESS else BackgroundMode.ERROR
                        }
                        recording -> BackgroundMode.RECORDING
                        loading -> BackgroundMode.PROCESSING
                        currentState.activeRecorder == null -> BackgroundMode.WAITING
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

        // Sync recorder state with UI widget
        combine(
            audioRecorder.isRecording,
            audioRecorder.isPaused,
            audioRecorder.durationMillis,
            audioRecorder.amplitudes,
            audioRecorder.playbackPositionMillis,
            audioRecorder.isPlaying
        ) { args: Array<Any> ->
            val recording = args[0] as Boolean
            val paused = args[1] as Boolean
            val duration = args[2] as Long
            @Suppress("UNCHECKED_CAST")
            val amplitudes = args[3] as List<Float>
            val playbackPos = args[4] as Long
            val playing = args[5] as Boolean

            if (recording || (duration > 0)) {
                updateRecorderWidget(recording, paused, duration, amplitudes, playbackPos, playing)
            }
        }
        .sample(32.milliseconds)
        .launchIn(scope)

        loadRecording()
        if (startRecording) {
            onMicClicked()
        }
    }

    private fun loadRecording() {
        launchSafe(
            block = {
                val result = subjectRepository.getSubject(subjectId)
                if (result is KmpResult.Success) {
                    _state.update { 
                        it.copy(
                            subject = result.data,
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
        val active = _state.value.activeRecorder
        if (active != null && active.status == UiWidget.VoiceRecorder.Status.RECORDING) {
            // Already recording, just let the Host expand the sheet
            return
        }
        startRecording()
    }

    fun toggleRecording() {
        if (audioRecorder.isRecording.value) {
            if (audioRecorder.isPaused.value) {
                audioRecorder.resume()
            } else {
                audioRecorder.pause()
            }
        } else {
            val active = _state.value.activeRecorder
            if (active != null && active.status == UiWidget.VoiceRecorder.Status.REVIEW) {
                onResumeRecording()
            } else {
                startRecording()
            }
        }
    }

    fun stopRecording() {
        val path = audioRecorder.stop()
        _state.update { currentState ->
            val active = currentState.activeRecorder
            if (active != null) {
                val finalDuration = audioRecorder.durationMillis.value
                currentState.copy(
                    activeRecorder = active.copy(
                        status = UiWidget.VoiceRecorder.Status.REVIEW,
                        filePath = path,
                        durationMillis = finalDuration,
                        trimEndMillis = finalDuration,
                        playbackPositionMillis = finalDuration // Ensure we stay at the end
                    )
                )
            } else {
                currentState
            }
        }
    }

    private fun startRecording() {
        audioRecorder.start()
        _state.update { 
            it.copy(
                activeRecorder = UiWidget.VoiceRecorder(id = "recorder_${nowAsEpochMilliseconds()}")
            )
        }
    }

    fun onSaveClicked() {
        if (audioRecorder.isRecording.value) {
            audioRecorder.stop()
        }
        _state.update { currentState ->
            val active = currentState.activeRecorder
            if (active != null) {
                currentState.copy(
                    activeRecorder = null,
                    widgets = currentState.widgets.filter { it !is UiWidget.WelcomeText } +
                            active.copy(status = UiWidget.VoiceRecorder.Status.FINISHED)
                )
            } else {
                currentState
            }
        }
    }

    fun onResumeRecording() {
        val active = _state.value.activeRecorder
        if (active != null && active.status == UiWidget.VoiceRecorder.Status.REVIEW) {
            audioRecorder.replace(active.durationMillis)
        } else {
            audioRecorder.resume()
        }
        
        _state.update { currentState ->
            currentState.copy(
                activeRecorder = currentState.activeRecorder?.copy(
                    status = UiWidget.VoiceRecorder.Status.RECORDING
                )
            )
        }
    }

    fun onTrimUpdate(start: Long, end: Long) {
        _state.update { currentState ->
            currentState.copy(
                activeRecorder = currentState.activeRecorder?.copy(
                    trimStartMillis = start,
                    trimEndMillis = end
                )
            )
        }
    }

    fun onSkip(millis: Long) {
        val current = audioRecorder.playbackPositionMillis.value
        audioRecorder.seekTo((current + millis).coerceIn(0, audioRecorder.durationMillis.value))
    }

    fun toggleExpand() {
        _state.update { currentState ->
            val active = currentState.activeRecorder ?: return@update currentState
            currentState.copy(
                activeRecorder = active.copy(isExpanded = !active.isExpanded)
            )
        }
    }

    fun onRecordingClicked(recorder: UiWidget.VoiceRecorder) {
        val path = recorder.filePath
        if (path != null) {
            audioRecorder.loadFile(path)
        }
        _state.update { 
            it.copy(
                activeRecorder = recorder.copy(
                    status = UiWidget.VoiceRecorder.Status.REVIEW,
                    isExpanded = false
                )
            )
        }
    }

    private fun updateRecorderWidget(
        recording: Boolean,
        paused: Boolean,
        duration: Long,
        amplitudes: List<Float>,
        playbackPos: Long,
        playing: Boolean
    ) {
        _state.update { currentState ->
            val currentActive = currentState.activeRecorder 
                ?: if (recording) UiWidget.VoiceRecorder(id = "recorder_sync_${nowAsEpochMilliseconds()}") else return@update currentState
            
            val newStatus = when {
                currentActive.status == UiWidget.VoiceRecorder.Status.FINISHED -> UiWidget.VoiceRecorder.Status.FINISHED
                currentActive.status == UiWidget.VoiceRecorder.Status.REVIEW -> UiWidget.VoiceRecorder.Status.REVIEW
                recording && paused -> UiWidget.VoiceRecorder.Status.PAUSED
                recording -> UiWidget.VoiceRecorder.Status.RECORDING
                duration > 0 -> UiWidget.VoiceRecorder.Status.REVIEW
                else -> currentActive.status
            }

            // Optimization: If we are paused, keep the current duration/amplitudes to avoid "jumping"
            // unless we are in sync mode.
            val shouldUpdateStats = recording || newStatus == UiWidget.VoiceRecorder.Status.REVIEW || newStatus == UiWidget.VoiceRecorder.Status.PAUSED

            currentState.copy(
                activeRecorder = currentActive.copy(
                    status = newStatus,
                    durationMillis = if (shouldUpdateStats) duration else currentActive.durationMillis,
                    amplitudes = if (shouldUpdateStats && amplitudes.isNotEmpty()) amplitudes else currentActive.amplitudes,
                    playbackPositionMillis = playbackPos,
                    isPlaying = playing
                )
            )
        }
    }

    fun onPlayClicked() {
        val active = _state.value.activeRecorder
        if (active?.status == UiWidget.VoiceRecorder.Status.PAUSED) {
            stopRecording()
            audioRecorder.play()
        } else {
            audioRecorder.play()
        }
    }

    fun onPausePlaybackClicked() {
        audioRecorder.pausePlayback()
    }

    fun onSeek(position: Long) {
        audioRecorder.seekTo(position)
    }

    fun onReplaceClicked() {
        val currentActive = state.value.activeRecorder ?: return
        audioRecorder.replace(currentActive.playbackPositionMillis)
        _state.update { currentState ->
            currentState.copy(
                activeRecorder = currentActive.copy(
                    status = UiWidget.VoiceRecorder.Status.RECORDING,
                    isReplacing = true
                )
            )
        }
    }

    fun onTrimCancel() {
        _state.update { currentState ->
            currentState.copy(
                activeRecorder = currentState.activeRecorder?.copy(
                    isTrimming = false
                )
            )
        }
    }

    fun onTrim(start: Long, end: Long) {
        launchSafe(
            block = {
                val newPath = audioRecorder.trim(start, end)
                if (newPath != null) {
                    _state.update { currentState ->
                        currentState.copy(
                            activeRecorder = currentState.activeRecorder?.copy(
                                filePath = newPath,
                                durationMillis = end - start,
                                isTrimming = false
                            )
                        )
                    }
                }
            }
        )
    }

    fun toggleTrimMode() {
        _state.update { currentState ->
            val active = currentState.activeRecorder ?: return@update currentState
            currentState.copy(
                activeRecorder = active.copy(
                    isTrimming = !active.isTrimming,
                    trimStartMillis = 0,
                    trimEndMillis = active.durationMillis
                )
            )
        }
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

    fun goBack() {
        navigation.back()
    }

    fun onUploadFromFileClicked() {
        // TODO: Implement file upload logic
    }
}
