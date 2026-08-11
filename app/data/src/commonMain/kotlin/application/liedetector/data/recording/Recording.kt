package application.liedetector.data.recording

import kotlinx.serialization.Serializable

@Serializable
data class Recording(
    val id: String,
    val title: String,
    val filePath: String,
    val durationMillis: Long,
    val amplitudes: List<Float> = emptyList(),
    val createdAt: Long
)
