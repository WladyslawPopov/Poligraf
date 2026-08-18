package application.poligraf.engine.error

class AppException(
    val type: ErrorType,
    override val message: String? = null,
    val code: String? = null
) : Throwable(message)

enum class ErrorType {
    UNAUTHORIZED,
    FORBIDDEN,
    NOT_FOUND,
    INVALID_REQUEST,
    SERVER_UNAVAILABLE,
    NO_INTERNET,
    UNKNOWN
}
