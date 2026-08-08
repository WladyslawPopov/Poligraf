package application.liedetector.navigation

import androidx.compose.runtime.Stable
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

/**
 * High-level navigation commands to be implemented by native platforms.
 */
@Stable
interface AppNavigation {
    fun openMain()
    fun openDebug()
    fun openRecording(subjectId: String)
    fun back()
    
    fun toggleDrawer()
    fun setDrawerOpen(isOpen: Boolean)
}
