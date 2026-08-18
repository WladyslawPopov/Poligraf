package application.poligraf.engine.utils

/**
 * A wrapper for KMP results to be easily handled in Swift.
 * Making Error generic ensures Swift can match it with the expected return type.
 */
sealed class RequestResult<out T> {
    data class Success<out T>(val data: T) : RequestResult<T>()
    data class Error<out T>(val throwable: Throwable) : RequestResult<T>()
}
