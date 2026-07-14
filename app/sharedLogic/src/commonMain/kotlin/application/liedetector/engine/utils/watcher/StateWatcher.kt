package application.liedetector.engine.utils.watcher

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * A wrapper for StateFlow to be easily observed from Swift.
 */
class StateWatcher<T>(
    private val flow: StateFlow<T>,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
) {
    /**
     * Called from Swift to start observing the state.
     * Returns a Closeable (Job wrapper) to cancel observation.
     */
    fun watch(block: (T) -> Unit): WatcherCloseable {
        val job = flow.onEach { block(it) }.launchIn(scope)
        return WatcherCloseable { job.cancel() }
    }

    /**
     * Get current value directly.
     */
    val value: T get() = flow.value
}

/**
 * A simple wrapper to handle job cancellation from Swift.
 */
fun interface WatcherCloseable {
    fun close()
}

/**
 * Extension to wrap any StateFlow into a Watcher.
 */
fun <T> StateFlow<T>.asWatcher(scope: CoroutineScope): StateWatcher<T> = StateWatcher(this, scope)
