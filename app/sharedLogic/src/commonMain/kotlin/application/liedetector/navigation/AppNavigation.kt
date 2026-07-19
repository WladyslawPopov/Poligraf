package application.liedetector.navigation

/**
 * High-level navigation commands to be implemented by native platforms.
 */
interface AppNavigation {
    fun openMain()
    fun openDebug()
    fun openInvestigation(subjectId: String)
    fun back()
    
    fun toggleDrawer()
    fun setDrawerOpen(isOpen: Boolean)
}
