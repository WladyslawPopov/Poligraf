package application.liedetector.models

/**
 * A wrapper for KMP results to be easily handled in Swift.
 * Making Error generic ensures Swift can match it with the expected return type.
 */
sealed class KmpResult<out T> {
    data class Success<out T>(val data: T) : KmpResult<T>()
    data class Error<out T>(val throwable: Throwable) : KmpResult<T>()
}
