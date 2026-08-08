package application.liedetector.engine.navigation

import androidx.compose.runtime.Stable

@Stable
interface AppNavigation {
    fun openMain()
    fun openDebug()
    fun openRecording(subjectId: String)
    fun back()
    
    fun toggleDrawer()
    fun setDrawerOpen(isOpen: Boolean)
}
