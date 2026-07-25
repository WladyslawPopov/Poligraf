package application.liedetector.engine.component

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import application.liedetector.navigation.AppNavigation
import application.liedetector.presentation.root.RootComponent
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.LifecycleRegistry
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner

/**
 * Factory for Swift to create the RootComponent.
 */
fun createRootComponent(navigation: AppNavigation): RootComponent {
    // Correct way to initialize Lifecycle on Native
    val lifecycle = LifecycleRegistry(object : LifecycleOwner {
        override val lifecycle: Lifecycle get() = throw IllegalStateException()
    })
    
    // Correct way to initialize SavedState on Native
    val controller = SavedStateRegistryController.create(object : SavedStateRegistryOwner {
        override val lifecycle: Lifecycle = lifecycle
        override val savedStateRegistry: SavedStateRegistry get() = throw IllegalStateException()
    })
    
    val context = DefaultComponentContext(
        lifecycle = lifecycle,
        viewModelStore = ViewModelStore(),
        savedStateRegistry = controller.savedStateRegistry,
    )
    
    lifecycle.currentState = Lifecycle.State.RESUMED
    
    return RootComponent(context, navigation)
}
