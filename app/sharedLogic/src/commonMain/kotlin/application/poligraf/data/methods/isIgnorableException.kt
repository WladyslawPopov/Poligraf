package application.poligraf.data.methods

import kotlin.coroutines.cancellation.CancellationException

/**
 * Filter for "noisy" network exceptions that shouldn't crash or block the UI.
 */
fun Throwable.isIgnorableException(): Boolean {
    val message = this.message ?: ""
    val name = this::class.simpleName ?: ""

    return this is CancellationException ||
            name.contains("Timeout", ignoreCase = true) ||
            message.contains("Timeout", ignoreCase = true) ||
            message.contains("Unable to resolve host", ignoreCase = true) ||
            message.contains("failed to connect to", ignoreCase = true) ||
            message.contains("Failed to connect", ignoreCase = true)
}
