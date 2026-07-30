package application.liedetector.uicore.types

import kotlinx.serialization.Serializable

@Serializable
enum class ErrorType {
    NO_INTERNET,
    SERVER_UNAVAILABLE,
    UNAUTHORIZED,
    UNKNOWN
}
