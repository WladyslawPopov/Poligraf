package application.liedetector.presentation.recordingHistory

import application.liedetector.data.base.BaseViewModel
import application.liedetector.data.subject.SubjectRepository
import application.liedetector.engine.io.audio.AudioRecorder
import application.liedetector.models.KmpResult
import application.liedetector.engine.navigation.AppNavigation
import application.liedetector.engine.utils.nowAsEpochMilliseconds
import application.liedetector.presentation.recording.data.RecordingState
import application.liedetector.domain.usecase.recording.DeleteRecordingUseCase
import application.liedetector.domain.usecase.recording.GetRecordingsUseCase
import application.liedetector.domain.usecase.recording.LoadRecordingsUseCase
import application.liedetector.domain.usecase.recording.SaveRecordingUseCase
import application.liedetector.uicore.theme.tokens.StringToken
import application.liedetector.uicore.types.BackgroundMode
import application.liedetector.uicore.types.ToastType
import application.liedetector.uicore.widgets.AppBackground
import application.liedetector.uicore.widgets.UiWidget
import io.github.aakira.napier.Napier
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlin.time.Duration.Companion.milliseconds
import application.liedetector.uicore.theme.tokens.*
import application.liedetector.uicore.widgets.VoiceRecorder
import application.liedetector.engine.utils.convertHoursAndMinutes
import application.liedetector.engine.utils.nowAsEpochSeconds

@OptIn(FlowPreview::class)
class RecordingsHistoryViewModel(
    private val subjectId: String,
    private val navigation: AppNavigation,
    private val subjectRepository: SubjectRepository,
    private val audioRecorder: AudioRecorder,
    private val getRecordingsUseCase: GetRecordingsUseCase,
    private val saveRecordingUseCase: SaveRecordingUseCase,
    private val deleteRecordingUseCase: DeleteRecordingUseCase,
    private val loadRecordingsUseCase: LoadRecordingsUseCase,
    startRecording: Boolean = false
) : BaseViewModel() {

    private val _state = MutableStateFlow(RecordingState())
    val state: StateFlow<RecordingState> = _state.asStateFlow()

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
                    _activeRecorder.value == null -> BackgroundMode.WAITING
                    else -> BackgroundMode.IDLE
                }
                
                if (currentBg.mode != newMode) {
                    _state.update { it.copy(background = currentBg.copy(mode = newMode)) }
                }
            }
        }.launchIn(scope)

        // Sync recorder state with UI widget (Essential for Android, ignored by iOS Native Engine)
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

            updateRecorderWidget(recording, paused, duration, amplitudes, playbackPos, playing)
        }
        .launchIn(scope)

        // Keep recorderUiState in sync with activeRecorder
        _activeRecorder
            .onEach { recorder -> syncRecorderUiState(recorder) }
            .launchIn(scope)

        // Collect recordings from repository
        getRecordingsUseCase(subjectId)
            .onEach { recordings ->
                _historicalRecordings.value = recordings.map { r -> r.toVoiceRecorder() }
            }
            .launchIn(scope)

        launchSafe(
            block = {
                loadRecordingsUseCase(subjectId)
            }
        )

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

    fun onSaveClicked() {
        launchSafe(
            block = {
                var active = _activeRecorder.value ?: return@launchSafe
                
                // ALWAYS stop and get LATEST path from engine
                val enginePath = audioRecorder.stop()
                if (!enginePath.isNullOrEmpty()) {
                    active = active.copy(filePath = enginePath)
                    Napier.i { "ViewModel: Received path from engine for saving: $enginePath" }
                } else if (active.filePath.isNullOrEmpty()) {
                    Napier.e { "ViewModel: CRITICAL - No path from engine AND no path in active recorder" }
                }
                
                val finalDuration = audioRecorder.durationMillis.value
                val finalAmplitudes = audioRecorder.amplitudes.value
                
                val recordingToSave = active.copy(
                    durationMillis = if (finalDuration > 0) finalDuration else active.durationMillis,
                    amplitudes = if (finalAmplitudes.isNotEmpty()) finalAmplitudes else active.amplitudes
                ).toRecording()

                saveRecordingUseCase(subjectId, recordingToSave)
                
                audioRecorder.cancel()
                _activeRecorder.value = null
            }
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

    fun onTrimUpdate(start: Long, end: Long) {
        _activeRecorder.update { active ->
            if (active == null) return@update null
            val duration = maxOf(100L, active.durationMillis)
            active.copy(
                trimStartMillis = start.coerceIn(0, duration),
                trimEndMillis = end.coerceIn(start + 10L, duration)
            )
        }
    }

    fun onSkip(millis: Long) {
        val current = audioRecorder.playbackPositionMillis.value
        audioRecorder.seekTo((current + millis).coerceIn(0, audioRecorder.durationMillis.value))
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

    fun onRecordingSaved(
        id: String,
        path: String,
        duration: Long,
        amplitudes: List<Float>
    ) {
        launchSafe(
            block = {
                val active = _activeRecorder.value
                val finalPath = path.ifEmpty {
                    val stoppedPath = if (audioRecorder.isRecording.value) audioRecorder.stop() else null
                    stoppedPath ?: active?.filePath
                } ?: ""

                val recorder = VoiceRecorder(
                    id = id,
                    title = active?.title ?: "Recording ${_historicalRecordings.value.size + 1}",
                    filePath = finalPath,
                    durationMillis = duration,
                    amplitudes = amplitudes,
                    status = VoiceRecorder.Status.FINISHED
                )
                saveRecordingUseCase(subjectId, recorder.toRecording())
                _activeRecorder.value = null
            }
        )
    }

    private fun updateRecorderWidget(
        recording: Boolean,
        paused: Boolean,
        duration: Long,
        amplitudes: List<Float>,
        playbackPos: Long,
        playing: Boolean
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

    fun onTrimCancel() {
        _activeRecorder.update { it?.copy(isTrimming = false) }
    }

    fun onTrim(start: Long, end: Long) {
        launchSafe(
            block = {
                val newPath = audioRecorder.trim(start, end)
                if (newPath != null) {
                    val updatedAmplitudes = audioRecorder.amplitudes.value
                    _activeRecorder.update { active ->
                        active?.copy(
                            filePath = newPath,
                            durationMillis = end - start,
                            amplitudes = updatedAmplitudes,
                            isTrimming = false,
                            status = VoiceRecorder.Status.REVIEW,
                            trimStartMillis = 0L,
                            trimEndMillis = end - start
                        )
                    }
                    // Reset engine state to the new trimmed file for immediate playback
                    audioRecorder.loadFile(newPath, updatedAmplitudes)
                }
            }
        )
    }

    fun toggleTrimMode() {
        _activeRecorder.update { active ->
            if (active == null) return@update null
            active.copy(
                isTrimming = !active.isTrimming,
                trimStartMillis = 0L,
                trimEndMillis = active.durationMillis
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

    fun handleAction(action: VoiceRecorderAction) {
        when (action) {
            is VoiceRecorderAction.ToggleExpand -> toggleExpand()
            is VoiceRecorderAction.ToggleRecord -> toggleRecording()
            is VoiceRecorderAction.TogglePlay -> onPlayClicked()
            is VoiceRecorderAction.StopRecording -> stopRecording()
            is VoiceRecorderAction.SaveRecording -> onSaveClicked()
            is VoiceRecorderAction.ToggleTrimMode -> toggleTrimMode()
            is VoiceRecorderAction.CancelTrim -> onTrimCancel()
            is VoiceRecorderAction.ApplyTrim -> onTrim(action.start, action.end)
            is VoiceRecorderAction.SeekTo -> onSeek(action.position)
            is VoiceRecorderAction.Skip -> onSkip(action.millis)
            is VoiceRecorderAction.UpdateTrimRange -> onTrimUpdate(action.start, action.end)
            is VoiceRecorderAction.UploadFromFile -> onUploadFromFileClicked()
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
        val ids = _selectedIds.value
        if (ids.isEmpty()) return
        
        launchSafe(
            isBlocking = true,
            block = {
                ids.forEach { id ->
                    deleteRecordingUseCase(subjectId, id)
                }
                _selectedIds.value = emptySet()
                _isSelectionMode.value = false
                showToast(StringToken.DELETE_RECORDING_CONFIRMATION, ToastType.SUCCESS)
            }
        )
    }

    private fun discardActiveRecording() {
        // При закрытии диктофона всегда сбрасываем состояние движка,
        // чтобы при следующем открытии все началось с нуля.
        audioRecorder.cancel()
        _activeRecorder.value = null
    }

    private fun deleteLocalRecording(id: String) {
        launchSafe(
            block = {
                deleteRecordingUseCase(subjectId, id)
            }
        )
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
