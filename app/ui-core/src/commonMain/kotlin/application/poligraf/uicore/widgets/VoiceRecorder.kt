package application.poligraf.uicore.widgets

import androidx.compose.runtime.Immutable

@Immutable
data class VoiceRecorder(
    val id: String,
    val status: Status = Status.IDLE,
    val durationMillis: Long = 0,
    val playbackPositionMillis: Long = 0,
    val amplitudes: List<Float> = emptyList(),
    val stressLevel: Float = 0f,
    val filePath: String? = null,
    val isPlaying: Boolean = false,
    val isTrimming: Boolean = false,
    val isReplacing: Boolean = false,
    val isExpanded: Boolean = false,
    val title: String = "New Recording",
    val trimStartMillis: Long = 0,
    val trimEndMillis: Long = 0
) {
    enum class Status { IDLE, RECORDING, PAUSED, REVIEW, FINISHED }
}
