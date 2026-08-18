package application.poligraf.models

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String? = null,
    val email: String? = null,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val subscriptionTier: String = "free",
    val metadata: Map<String, String> = emptyMap() // For device info, locale, etc.
)
