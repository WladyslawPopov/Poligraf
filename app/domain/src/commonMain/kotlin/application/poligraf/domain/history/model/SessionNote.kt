package application.poligraf.domain.history.model

import kotlinx.serialization.Serializable

@Serializable
data class SessionNote(
    val id: String,
    val sessionId: String,
    val timestamp: Long,
    val text: String,
    val markerColor: String? = null,
    val markerShape: String? = null
)
