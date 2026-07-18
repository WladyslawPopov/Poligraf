package application.liedetector.uicore.state

import application.liedetector.uicore.theme.StringToken
import kotlinx.serialization.Serializable

@Serializable
enum class ErrorType {
    NO_INTERNET,
    SERVER_UNAVAILABLE,
    UNAUTHORIZED,
    UNKNOWN
}

@Serializable
enum class ToastType {
    SUCCESS,
    WARNING,
    ERROR
}

@Serializable
data class ToastState(
    val messageToken: StringToken? = null,
    val messageRaw: String? = null,
    val type: ToastType
)
