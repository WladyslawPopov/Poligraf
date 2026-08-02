package application.liedetector.models

import kotlinx.serialization.Serializable

@Serializable
data class ServerStatus(
    val status: String,
    val version: String = "1.0.0"
)

@Serializable
data class AnalysisRequest(
    val storagePath: String,
    val contextText: String,
    val subjectId: String? = null
)

@Serializable
data class AnalysisResponse(
    val id: String,
    val verdict: String,
    val deceptionProbability: Int,
    val acousticStressScore: Int,
    val manipulationScore: Int,
    val logicConsistencyScore: Int,
    val reasoning: String,
    val createdAt: String
)

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

@Serializable
data class UserDto(
    val id: String? = null,
    val email: String? = null,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val subscriptionTier: String = "free",
    val metadata: Map<String, String> = emptyMap() // For device info, locale, etc.
)

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
