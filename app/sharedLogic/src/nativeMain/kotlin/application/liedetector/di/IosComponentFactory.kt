package application.liedetector.di

import application.liedetector.navigation.DefaultBackPressedHandler
import application.liedetector.navigation.DefaultNavigationContext
import application.liedetector.presentation.root.RootComponent
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.LifecycleRegistry
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController

/**
 * Factory for Swift to create the RootComponent.
 */
fun createRootComponent(): RootComponent {
    // Correct way to initialize Lifecycle on Native
    val lifecycle = LifecycleRegistry(object : androidx.lifecycle.LifecycleOwner {
        override val lifecycle: androidx.lifecycle.Lifecycle get() = throw IllegalStateException()
    })
    
    // Correct way to initialize SavedState on Native
    val controller = SavedStateRegistryController.create(object : androidx.savedstate.SavedStateRegistryOwner {
        override val lifecycle: androidx.lifecycle.Lifecycle = lifecycle
        override val savedStateRegistry: SavedStateRegistry get() = throw IllegalStateException()
    })
    
    val context = DefaultNavigationContext(
        lifecycle = lifecycle,
        viewModelStore = ViewModelStore(),
        savedStateRegistry = controller.savedStateRegistry,
        backPressedHandler = DefaultBackPressedHandler()
    )
    return RootComponent(context)
}
