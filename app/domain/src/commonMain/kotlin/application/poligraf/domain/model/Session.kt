package application.poligraf.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Session(
    val id: String,
    val timestamp: Long,
    val title: String,
    val notes: String,
    val duration: Long,
    val isCompleted: Boolean,
    val anomalyCount: Int = 0
)
