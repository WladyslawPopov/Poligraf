package application.liedetector.component

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import application.liedetector.navigation.AppNavigation
import application.liedetector.presentation.root.RootComponent

/**
 * Factory for Swift to create the RootComponent.
 */
fun createRootComponent(navigation: AppNavigation): RootComponent {
    // Correct way to initialize Lifecycle on Native
    val lifecycle = LifecycleRegistry(object : LifecycleOwner {
        override val lifecycle: Lifecycle get() = throw IllegalStateException("Do not access lifecycle directly")
    })
    
    // Correct way to initialize SavedState on Native
    val controller = SavedStateRegistryController.create(object : SavedStateRegistryOwner {
        override val lifecycle: Lifecycle = lifecycle
        override val savedStateRegistry: SavedStateRegistry get() = throw IllegalStateException("Do not access registry directly")
    })
    
    val context = DefaultComponentContext(
        lifecycle = lifecycle,
        viewModelStore = ViewModelStore(),
        savedStateRegistry = controller.savedStateRegistry,
    )
    
    lifecycle.currentState = Lifecycle.State.RESUMED
    
    return RootComponent(context, navigation)
}
