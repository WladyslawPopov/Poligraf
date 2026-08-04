package application.liedetector.models

import kotlinx.serialization.Serializable

@Serializable
data class ServerStatus(
    val status: String,
    val version: String = "1.0.0"
)

@Serializable
data class ApiErrorResponse(
    val message: String,
    val code: String? = null,
    val details: Map<String, String>? = null
)
