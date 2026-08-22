package application.poligraf.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val name: String,
    val avatarEmoji: String = "👤",
    val isPro: Boolean = false,
    val preferences: UserPreferences = UserPreferences()
)

@Serializable
data class UserPreferences(
    val selectedSkin: String = "EQUALIZER",
    val selectedMarkerSet: String = "DEFAULT",
    val isHapticEnabled: Boolean = true
)
