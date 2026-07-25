package application.liedetector.navigation

import androidx.compose.runtime.Stable

/**
 * High-level navigation commands to be implemented by native platforms.
 */
@Stable
interface AppNavigation {
    fun openMain()
    fun openDebug()
    fun openInvestigation(subjectId: String)
    fun back()
    
    fun toggleDrawer()
    fun setDrawerOpen(isOpen: Boolean)
}
