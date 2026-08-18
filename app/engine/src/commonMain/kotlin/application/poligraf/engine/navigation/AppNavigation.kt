package application.poligraf.engine.navigation

import androidx.compose.runtime.Stable

import kotlinx.coroutines.flow.StateFlow

@Stable
interface AppNavigation {
    val isDrawerOpen: StateFlow<Boolean>
    
    fun openMain()
    fun openDebug()
    fun openRecording(subjectId: String)
    fun openRecordingsHistory(subjectId: String, startRecording: Boolean = false)
    fun back()
    
    fun toggleDrawer()
    fun setDrawerOpen(isOpen: Boolean)
}
