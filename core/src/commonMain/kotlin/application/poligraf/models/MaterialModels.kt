package application.poligraf.models

import kotlinx.serialization.Serializable

@Serializable
data class MaterialDto(
    val id: String? = null,
    val subjectId: String,
    val type: MaterialType,
    val storagePath: String? = null,
    val content: String? = null, // For text evidence
    val createdAt: String? = null
)

@Serializable
enum class MaterialType {
    AUDIO, IMAGE, TEXT
}
