package application.liedetector.models

import kotlinx.serialization.Serializable


sealed class KmpResult<out T> {
    data class Success<out T>(val data: T) : KmpResult<T>()
    data class Error<out T>(val throwable: Throwable) : KmpResult<T>()
}

@Serializable
data class ApiErrorResponse(
    val message: String,
    val code: String? = null,
    val details: Map<String, String>? = null
)
