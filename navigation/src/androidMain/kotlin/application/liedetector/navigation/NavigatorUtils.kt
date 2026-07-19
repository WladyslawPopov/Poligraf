package application.liedetector.navigation

import androidx.activity.ComponentActivity

/**
 * Extension to create NavigationContext from Activity
 */
fun ComponentActivity.navigationContext(): NavigationContext {
    return DefaultNavigationContext(
        lifecycle = this.lifecycle,
        viewModelStore = this.viewModelStore,
        savedStateRegistry = this.savedStateRegistry
    )
}
