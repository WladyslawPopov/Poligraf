package application.poligraf.navigation

import androidx.compose.runtime.Stable
import application.poligraf.data.AppRoute
import application.poligraf.engine.navigation.AppNavigation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Stable
class SharedNavigator : AppNavigation {
    
    private val _currentRoute = MutableStateFlow<AppRoute>(AppRoute.Main)
    val currentRoute = _currentRoute.asStateFlow()
    
    private val backStack = mutableListOf<AppRoute>(AppRoute.Main)

    private val _isDrawerOpen = MutableStateFlow(false)
    override val isDrawerOpen = _isDrawerOpen.asStateFlow()

    override fun openMain() {
        navigate(AppRoute.Main, clearStack = true)
    }

    override fun openDebug() {
        navigate(AppRoute.Debug)
    }

    override fun openRecording(subjectId: String) {
        navigate(AppRoute.Recording(subjectId))
    }

    override fun openRecordingsHistory(subjectId: String, startRecording: Boolean) {
        navigate(AppRoute.RecordingsHistory(subjectId, startRecording))
    }

    override fun back() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.size - 1)
            _currentRoute.value = backStack.last()
        }
    }

    override fun toggleDrawer() {
        _isDrawerOpen.update { !it }
    }

    override fun setDrawerOpen(isOpen: Boolean) {
        _isDrawerOpen.value = isOpen
    }

    private fun navigate(route: AppRoute, clearStack: Boolean = false) {
        if (clearStack) {
            backStack.clear()
        }
        backStack.add(route)
        _currentRoute.value = route
    }
}
