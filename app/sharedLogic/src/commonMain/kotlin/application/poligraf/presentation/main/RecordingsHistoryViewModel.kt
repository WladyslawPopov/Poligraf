package application.poligraf.presentation.main

import application.poligraf.presentation.base.BaseViewModel
import application.poligraf.engine.io.audio.AudioRecorder
import application.poligraf.engine.utils.convertHoursAndMinutes
import application.poligraf.engine.utils.nowAsEpochMilliseconds
import application.poligraf.engine.utils.nowAsEpochSeconds
import application.poligraf.uicore.state.VoiceRecorderAction
import application.poligraf.uicore.state.VoiceRecorderControlsState
import application.poligraf.uicore.state.VoiceRecorderHeaderState
import application.poligraf.uicore.state.VoiceRecorderTrimState
import application.poligraf.uicore.state.VoiceRecorderUiState
import application.poligraf.uicore.state.VoiceRecorderWaveformState
import application.poligraf.uicore.theme.tokens.ColorToken
import application.poligraf.uicore.theme.tokens.IconToken
import application.poligraf.uicore.widgets.VoiceRecorder
import io.github.aakira.napier.Napier
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
class RecordingsHistoryViewModel(
    private val subjectId: String,
    private val audioRecorder: AudioRecorder,
    startRecording: Boolean = false
) : BaseViewModel() {

    private val _recorderUiState = MutableStateFlow(VoiceRecorderUiState())
    val recorderUiState = _recorderUiState.asStateFlow()

    private val _activeRecorder = MutableStateFlow<VoiceRecorder?>(null)
    val activeRecorder = _activeRecorder.asStateFlow()

    private val _historicalRecordings = MutableStateFlow<List<VoiceRecorder>>(emptyList())
    val historicalRecordings = _historicalRecordings.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode = _isSelectionMode.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedIds = _selectedIds.asStateFlow()

    init {
        // Sync recorder state with UI widget (Essential for Android, ignored by iOS Native Engine)
        combine(
            audioRecorder.isRecording,
            audioRecorder.isPaused,
            audioRecorder.durationMillis,
            audioRecorder.amplitudes,
            audioRecorder.playbackPositionMillis,
            audioRecorder.isPlaying,
            audioRecorder.stressLevel
        ) { args: Array<Any> ->
            val recording = args[0] as Boolean
            val paused = args[1] as Boolean
            val duration = args[2] as Long

            @Suppress("UNCHECKED_CAST")
            val amplitudes = args[3] as List<Float>
            val playbackPos = args[4] as Long
            val playing = args[5] as Boolean
            val stress = args[6] as Float

            updateRecorderWidget(
                recording,
                paused,
                duration,
                amplitudes,
                playbackPos,
                playing,
                stress
            )
        }
        .launchIn(scope)

        // Keep recorderUiState in sync with activeRecorder
        _activeRecorder
            .onEach { recorder -> syncRecorderUiState(recorder) }
            .launchIn(scope)


        loadRecording()
        // Handle explicit start request
        if (startRecording) {
            startNewRecording(expand = true)
        } else {
            // Ensure any existing recording is NOT expanded by default
            _activeRecorder.update { it?.copy(isExpanded = false) }
        }
    }

    private fun loadRecording() {

    }

    fun onMicClicked() {
        if (_activeRecorder.value?.status == VoiceRecorder.Status.RECORDING) {
            // Already recording, just let the Host expand the sheet
            return
        }
        startNewRecording()
    }

    fun toggleRecording() {
        val active = _activeRecorder.value
        if (audioRecorder.isRecording.value) {
            if (audioRecorder.isPaused.value) {
                audioRecorder.resume()
            } else {
                audioRecorder.pause()
            }
        } else {
            if (active != null && active.status == VoiceRecorder.Status.REVIEW) {
                onResumeRecording()
            } else {
                startNewRecording()
            }
        }

        // Sync macro status for UI observers
        val newStatus = when {
            audioRecorder.isRecording.value && audioRecorder.isPaused.value -> VoiceRecorder.Status.PAUSED
            audioRecorder.isRecording.value -> VoiceRecorder.Status.RECORDING
            else -> active?.status ?: VoiceRecorder.Status.IDLE
        }
        _activeRecorder.update { it?.copy(status = newStatus) }
    }

    fun stopRecording() {
        launchSafe(
            block = {
                val path = audioRecorder.stop()
                _activeRecorder.update { active ->
                    active?.copy(
                        status = VoiceRecorder.Status.REVIEW,
                        filePath = path ?: active.filePath
                    )
                }
            }
        )
    }

    private fun startNewRecording(expand: Boolean = false) {
        audioRecorder.start()
        _activeRecorder.value = VoiceRecorder(
            id = "recorder_${nowAsEpochMilliseconds()}",
            isExpanded = expand,
            title = "Recording ${_historicalRecordings.value.size + 1}",
            status = VoiceRecorder.Status.RECORDING
        )
    }

    fun onResumeRecording() {
        val active = _activeRecorder.value
        if (active != null) {
            when (active.status) {
                VoiceRecorder.Status.REVIEW, VoiceRecorder.Status.PAUSED -> {
                    // If we were reviewing or paused, we use replace/resume logic
                    if (active.status == VoiceRecorder.Status.REVIEW) {
                        // Append to the end
                        audioRecorder.replace(active.durationMillis)
                    } else {
                        audioRecorder.resume()
                    }
                }
                else -> {
                    if (!audioRecorder.isRecording.value) {
                        startNewRecording()
                    }
                }
            }
        } else {
            startNewRecording()
        }

        _activeRecorder.update { it?.copy(status = VoiceRecorder.Status.RECORDING) }
    }


    fun toggleExpand() {
        _activeRecorder.update { it?.copy(isExpanded = !(it.isExpanded)) }
    }

    fun onRecordingClicked(recorder: VoiceRecorder) {
        launchSafe(
            block = {
                if (audioRecorder.isRecording.value) {
                    audioRecorder.stop()
                }
                audioRecorder.pausePlayback()
                delay(50.milliseconds)

                val path = recorder.filePath
                if (!path.isNullOrEmpty()) {
                    Napier.i { "ViewModel: Delegating load to engine for path: $path" }
                    audioRecorder.loadFile(path, recorder.amplitudes)
                } else {
                    Napier.e { "ViewModel: CANNOT LOAD. Path is NULL or EMPTY for ID=${recorder.id}" }
                }

                val updatedRecorder = recorder.copy(
                    status = VoiceRecorder.Status.REVIEW,
                    isExpanded = true,
                    playbackPositionMillis = 0L,
                    isPlaying = false
                )
                _activeRecorder.value = updatedRecorder
                syncRecorderUiState(updatedRecorder)
            }
        )
    }


    private fun updateRecorderWidget(
        recording: Boolean,
        paused: Boolean,
        duration: Long,
        amplitudes: List<Float>,
        playbackPos: Long,
        playing: Boolean,
        stress: Float
    ) {
        val currentActive = _activeRecorder.value
            ?: if (recording) {
                val newRec = VoiceRecorder(id = "recorder_sync_${nowAsEpochMilliseconds()}")
                _activeRecorder.value = newRec
                newRec
            } else return

        val newStatus = when {
            recording && paused -> VoiceRecorder.Status.PAUSED
            recording -> VoiceRecorder.Status.RECORDING
            currentActive.status == VoiceRecorder.Status.FINISHED -> VoiceRecorder.Status.FINISHED
            duration > 0 && !recording -> VoiceRecorder.Status.REVIEW
            else -> currentActive.status
        }

        _activeRecorder.value = currentActive.copy(
            status = newStatus,
            durationMillis = if (duration > 0) duration else currentActive.durationMillis,
            amplitudes = if (amplitudes.isNotEmpty()) amplitudes else currentActive.amplitudes,
            stressLevel = stress,
            playbackPositionMillis = playbackPos,
            isPlaying = playing
        )
    }

    fun onPlayClicked() {
        val active = _activeRecorder.value
        if (active?.isPlaying == true) {
            audioRecorder.pausePlayback()
        } else {
            launchSafe(
                block = {
                    if (active?.status == VoiceRecorder.Status.PAUSED || audioRecorder.isRecording.value) {
                        audioRecorder.stop()
                    }
                    audioRecorder.play()
                }
            )
        }
    }

    fun onPausePlaybackClicked() {
        audioRecorder.pausePlayback()
    }

    fun onSeek(position: Long) {
        audioRecorder.seekTo(position)
    }

    fun onReplaceClicked() {
        val currentActive = _activeRecorder.value ?: return
        audioRecorder.replace(currentActive.playbackPositionMillis)
        _activeRecorder.update { it?.copy(status = VoiceRecorder.Status.RECORDING) }
    }

    fun goBack() {
    }

    fun handleAction(action: VoiceRecorderAction) {
        when (action) {
            is VoiceRecorderAction.ToggleExpand -> toggleExpand()
            is VoiceRecorderAction.ToggleRecord -> toggleRecording()
            is VoiceRecorderAction.TogglePlay -> onPlayClicked()
            is VoiceRecorderAction.StopRecording -> stopRecording()
            is VoiceRecorderAction.SaveRecording -> {}
            is VoiceRecorderAction.ToggleTrimMode -> {}
            is VoiceRecorderAction.CancelTrim -> {}
            is VoiceRecorderAction.ApplyTrim -> {}
            is VoiceRecorderAction.SeekTo -> onSeek(action.position)
            is VoiceRecorderAction.Skip -> {}
            is VoiceRecorderAction.UpdateTrimRange -> {}
            is VoiceRecorderAction.UploadFromFile -> {}
            is VoiceRecorderAction.DeleteRecording -> deleteLocalRecording(action.id)
            is VoiceRecorderAction.DiscardActive -> discardActiveRecording()
            is VoiceRecorderAction.ToggleSelectionMode -> toggleSelectionMode()
            is VoiceRecorderAction.ToggleItemSelection -> toggleItemSelection(action.id)
            is VoiceRecorderAction.DeleteSelected -> deleteSelectedRecordings()
            is VoiceRecorderAction.ClearSelection -> clearSelection()
        }
    }

    private fun toggleSelectionMode() {
        _isSelectionMode.update { !it }
        if (!_isSelectionMode.value) {
            _selectedIds.value = emptySet()
        }
    }

    private fun toggleItemSelection(id: String) {
        _selectedIds.update { current ->
            if (current.contains(id)) current - id else current + id
        }
    }

    private fun clearSelection() {
        _selectedIds.value = emptySet()
        _isSelectionMode.value = false
    }

    private fun deleteSelectedRecordings() {

    }

    private fun discardActiveRecording() {
        audioRecorder.cancel()
        _activeRecorder.value = null
    }

    private fun deleteLocalRecording(id: String) {

    }

    private fun syncRecorderUiState(recorder: VoiceRecorder?) {
        if (recorder == null) {
            _recorderUiState.value = VoiceRecorderUiState()
            return
        }

        val isRecording = recorder.status == VoiceRecorder.Status.RECORDING
        val isPaused = recorder.status == VoiceRecorder.Status.PAUSED
        val isReview = recorder.status == VoiceRecorder.Status.REVIEW || recorder.status == VoiceRecorder.Status.FINISHED

        val timerValue = if (isReview || recorder.isTrimming || isPaused)
            recorder.playbackPositionMillis
        else
            recorder.durationMillis

        // Smart split for Title and Time/Date
        val displayTitle: String
        val displaySubtitle: String
        val titleMatch = Regex("(.+)\\s(\\d{2}:\\d{2})$").find(recorder.title)
        if (titleMatch != null) {
            displayTitle = titleMatch.groupValues[1]
            displaySubtitle = titleMatch.groupValues[2]
        } else {
            displayTitle = recorder.title
            displaySubtitle = if (isRecording) "Recording..." else formatTimeShort()
        }

        val newState = VoiceRecorderUiState(
            id = recorder.id,
            filePath = recorder.filePath,
            isExpanded = recorder.isExpanded,
            header = VoiceRecorderHeaderState(
                title = displayTitle,
                subtitle = displaySubtitle,
                timerLabel = formatDurationSimple(recorder.durationMillis),
                timerLabelPrecise = formatDurationPrecise(timerValue),
                isMenuVisible = !recorder.isTrimming,
                isSaveVisible = !recorder.isTrimming,
                isTrimming = recorder.isTrimming,
                accentColor = ColorToken.RECORDER_SECONDARY
            ),
            waveform = VoiceRecorderWaveformState(
                amplitudes = recorder.amplitudes,
                latestAmplitude = recorder.amplitudes.lastOrNull() ?: 0f,
                stressLevel = recorder.stressLevel,
                durationMillis = recorder.durationMillis,
                playbackPositionMillis = recorder.playbackPositionMillis,
                isRecording = isRecording,
                isPaused = isPaused,
                isTrimming = recorder.isTrimming,
                trimStartMillis = recorder.trimStartMillis,
                trimEndMillis = recorder.trimEndMillis,
                primaryColor = ColorToken.RECORDER_PRIMARY,
                secondaryColor = ColorToken.RECORDER_SECONDARY,
                backgroundColor = ColorToken.RECORDER_SURFACE,
                rulerColor = ColorToken.TEXT_PRIMARY
            ),
            controls = VoiceRecorderControlsState(
                collapsedIcon = when {
                    isRecording -> IconToken.PAUSE
                    isPaused -> IconToken.MIC
                    recorder.isPlaying -> IconToken.PAUSE
                    else -> IconToken.PLAY
                },
                collapsedButtonColor = if (isRecording || isPaused) ColorToken.RECORDER_PRIMARY else ColorToken.RECORDER_SECONDARY,
                playbackIcon = if (recorder.isPlaying) IconToken.PAUSE else IconToken.PLAY,
                recordIcon = if (isRecording) IconToken.PAUSE else IconToken.MIC,
                isPlaying = recorder.isPlaying,
                isRecording = isRecording,
                recordButtonColor = ColorToken.RECORDER_PRIMARY,
                skipBackIcon = IconToken.SKIP_BACK_15,
                skipForwardIcon = IconToken.SKIP_FORWARD_15
            ),
            trim = VoiceRecorderTrimState(
                isVisible = recorder.isTrimming,
                startMillis = recorder.trimStartMillis,
                endMillis = recorder.trimEndMillis,
                durationMillis = recorder.durationMillis,
                playbackPositionMillis = recorder.playbackPositionMillis,
                frameColor = ColorToken.RECORDER_ACCENT,
                handleIconLeft = IconToken.TRIM_HANDLE_LEFT,
                handleIconRight = IconToken.TRIM_HANDLE_RIGHT
            ),
            surfaceColor = ColorToken.RECORDER_SURFACE
        )

        _recorderUiState.value = newState
    }

    private fun formatDurationPrecise(millis: Long): String {
        val ms = (millis % 1000) / 10
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')},${ms.toString().padStart(2, '0')}"
    }

    private fun formatDurationSimple(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60))
        return "$minutes:${seconds.toString().padStart(2, '0')}"
    }

    private fun formatTimeShort(): String {
        return nowAsEpochSeconds().convertHoursAndMinutes()
    }
}
