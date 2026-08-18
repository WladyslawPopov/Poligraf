package application.poligraf.models

import kotlinx.serialization.Serializable

@Serializable
data class SubjectDto(
    val id: String? = null,
    val name: String,
    val avatar: String? = null, // Emoji or URL
    val isDefaultAvatar: Boolean = true,
    val description: String? = null,
    val isPublic: Boolean = false,
    val metadata: Map<String, String> = emptyMap()
)
