package application.poligraf.data.methods

import application.poligraf.engine.error.ErrorType

/**
 * Maps raw exceptions to high-level ErrorType for consistent UI rendering.
 */
fun Throwable.toErrorType(): ErrorType {
    val message = this.message ?: ""
    return when {
        message.contains("Unable to resolve host", ignoreCase = true) || 
        message.contains("failed to connect", ignoreCase = true) -> ErrorType.NO_INTERNET
        else -> ErrorType.UNKNOWN
    }
}
