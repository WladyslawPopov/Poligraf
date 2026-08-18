package application.poligraf.engine.config

/**
 * Application-wide configuration provided at startup.
 * Stores platform-specific info like versions and IDs.
 */
data class AppConfig(
    val appVersion: String,
    val deviceId: String,
    val isDebug: Boolean = false,
    val platform: String // "Android" or "iOS"
)
