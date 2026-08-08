package application.liedetector.engine.component

import androidx.navigation.NavBackStackEntry

fun NavBackStackEntry.componentContext(): ComponentContext {
    return DefaultComponentContext(
        lifecycle = this.lifecycle,
        viewModelStore = this.viewModelStore,
        savedStateRegistry = this.savedStateRegistry
    )
}
