package application.liedetector.navigation

import androidx.compose.runtime.Stable
import application.liedetector.engine.navigation.AppNavigation
import application.liedetector.engine.navigation.NavigationGlobalLock
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class NavEvent {
    data object OpenMain : NavEvent()
    data object OpenDebug : NavEvent()
    data class OpenRecording(val subjectId: String) : NavEvent()
    data object Back : NavEvent()
}

@Stable
class AndroidNavigator : AppNavigation {
    
    private val _navigationEvents = MutableSharedFlow<NavEvent>(extraBufferCapacity = 1)
    val navigationEvents = _navigationEvents.asSharedFlow()
    
    private val _isDrawerOpen = MutableStateFlow(false)
    val isDrawerOpen = _isDrawerOpen.asStateFlow()

    override fun openMain() {
        if (NavigationGlobalLock.canNavigate()) {
            _navigationEvents.tryEmit(NavEvent.OpenMain)
        }
    }

    override fun openDebug() {
        if (NavigationGlobalLock.canNavigate()) {
            _navigationEvents.tryEmit(NavEvent.OpenDebug)
        }
    }

    override fun openRecording(subjectId: String) {
        if (NavigationGlobalLock.canNavigate()) {
            _navigationEvents.tryEmit(NavEvent.OpenRecording(subjectId))
        }
    }

    override fun back() {
        _navigationEvents.tryEmit(NavEvent.Back)
    }

    override fun toggleDrawer() {
        _isDrawerOpen.value = !_isDrawerOpen.value
    }

    override fun setDrawerOpen(isOpen: Boolean) {
        _isDrawerOpen.value = isOpen
    }
}
