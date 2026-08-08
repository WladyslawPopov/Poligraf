package application.liedetector.engine.navigation

import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

/**
 * Global lock to prevent rapid multiple navigation clicks (anti-spam).
 */
object NavigationGlobalLock {
    private var lastNavMark: TimeSource.Monotonic.ValueTimeMark? = null
    private val debounceDuration = 500.milliseconds

    fun canNavigate(): Boolean {
        val now = TimeSource.Monotonic.markNow()
        val lastMark = lastNavMark
        
        return if (lastMark == null || (now - lastMark) > debounceDuration) {
            lastNavMark = now
            true
        } else {
            false
        }
    }
}
