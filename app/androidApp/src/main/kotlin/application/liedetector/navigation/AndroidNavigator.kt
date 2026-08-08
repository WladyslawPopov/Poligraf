package application.liedetector.navigation

import androidx.compose.runtime.Stable
import androidx.navigation.NavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Stable
class AndroidNavigator : AppNavigation {
    
    private var navController: NavController? = null
    
    private val _isDrawerOpen = MutableStateFlow(false)
    val isDrawerOpen = _isDrawerOpen.asStateFlow()

    fun bind(navController: NavController) {
        this.navController = navController
    }

    fun unbind() {
        this.navController = null
    }

    override fun openMain() {
        if (NavigationGlobalLock.canNavigate()) {
            navController?.navigate(AppRoute.Main) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    override fun openDebug() {
        if (NavigationGlobalLock.canNavigate()) {
            navController?.navigate(AppRoute.Debug) {
                launchSingleTop = true
            }
        }
    }

    override fun openRecording(subjectId: String) {
        if (NavigationGlobalLock.canNavigate()) {
            navController?.navigate(AppRoute.Recording(subjectId)) {
                launchSingleTop = true
            }
        }
    }

    override fun back() {
        navController?.popBackStack()
    }

    override fun toggleDrawer() {
        _isDrawerOpen.value = !_isDrawerOpen.value
    }

    override fun setDrawerOpen(isOpen: Boolean) {
        _isDrawerOpen.value = isOpen
    }
}
