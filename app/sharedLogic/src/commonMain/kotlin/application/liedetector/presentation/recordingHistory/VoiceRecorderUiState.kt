package application.liedetector.presentation.recordingHistory

import androidx.compose.runtime.Immutable
import application.liedetector.uicore.theme.tokens.ColorToken
import application.liedetector.uicore.theme.tokens.IconToken

@Immutable
data class VoiceRecorderUiState(
    val id: String = "",
    val filePath: String? = null,
    val isExpanded: Boolean = false,
    val header: VoiceRecorderHeaderState = VoiceRecorderHeaderState(),
    val waveform: VoiceRecorderWaveformState = VoiceRecorderWaveformState(),
    val controls: VoiceRecorderControlsState = VoiceRecorderControlsState(),
    val trim: VoiceRecorderTrimState = VoiceRecorderTrimState(),
    val surfaceColor: ColorToken = ColorToken.RECORDER_SURFACE
)

@Immutable
data class VoiceRecorderHeaderState(
    val title: String = "",
    val subtitle: String = "",
    val timerLabel: String = "00:00",
    val timerLabelPrecise: String = "00:00,00",
    val isMenuVisible: Boolean = true,
    val isSaveVisible: Boolean = true,
    val isTrimming: Boolean = false,
    val accentColor: ColorToken = ColorToken.RECORDER_SECONDARY
)

@Immutable
data class VoiceRecorderWaveformState(
    val amplitudes: List<Float> = emptyList(),
    val latestAmplitude: Float = 0f,
    val durationMillis: Long = 0,
    val playbackPositionMillis: Long = 0,
    val scrollOffset: Float = 0f,
    val isRecording: Boolean = false,
    val isPaused: Boolean = false,
    val isTrimming: Boolean = false,
    val trimStartMillis: Long = 0,
    val trimEndMillis: Long = 0,
    val primaryColor: ColorToken = ColorToken.RECORDER_PRIMARY,
    val secondaryColor: ColorToken = ColorToken.RECORDER_SECONDARY,
    val backgroundColor: ColorToken = ColorToken.RECORDER_SURFACE,
    val rulerColor: ColorToken = ColorToken.TEXT_PRIMARY
)

@Immutable
data class VoiceRecorderControlsState(
    val collapsedIcon: IconToken = IconToken.PLAY,
    val collapsedButtonColor: ColorToken = ColorToken.RECORDER_SECONDARY,
    val playbackIcon: IconToken = IconToken.PLAY,
    val recordIcon: IconToken = IconToken.MIC,
    val isPlaying: Boolean = false,
    val isRecording: Boolean = false,
    val recordButtonColor: ColorToken = ColorToken.RECORDER_PRIMARY,
    val skipBackIcon: IconToken = IconToken.HISTORY,
    val skipForwardIcon: IconToken = IconToken.HISTORY
)

@Immutable
data class VoiceRecorderTrimState(
    val isVisible: Boolean = false,
    val startMillis: Long = 0,
    val endMillis: Long = 0,
    val durationMillis: Long = 0,
    val playbackPositionMillis: Long = 0,
    val frameColor: ColorToken = ColorToken.RECORDER_ACCENT,
    val handleIconLeft: IconToken = IconToken.TRIM_HANDLE_LEFT,
    val handleIconRight: IconToken = IconToken.TRIM_HANDLE_RIGHT
)

sealed class VoiceRecorderAction {
    object ToggleExpand : VoiceRecorderAction()
    object ToggleRecord : VoiceRecorderAction()
    object TogglePlay : VoiceRecorderAction()
    object StopRecording : VoiceRecorderAction()
    object SaveRecording : VoiceRecorderAction()
    object ToggleTrimMode : VoiceRecorderAction()
    object CancelTrim : VoiceRecorderAction()
    data class ApplyTrim(val start: Long, val end: Long) : VoiceRecorderAction()
    data class SeekTo(val position: Long) : VoiceRecorderAction()
    data class Skip(val millis: Long) : VoiceRecorderAction()
    data class UpdateTrimRange(val start: Long, val end: Long) : VoiceRecorderAction()
    object UploadFromFile : VoiceRecorderAction()
    data class DeleteRecording(val id: String) : VoiceRecorderAction()
    object DiscardActive : VoiceRecorderAction()
    object ToggleSelectionMode : VoiceRecorderAction()
    data class ToggleItemSelection(val id: String) : VoiceRecorderAction()
    object DeleteSelected : VoiceRecorderAction()
    object ClearSelection : VoiceRecorderAction()
}
