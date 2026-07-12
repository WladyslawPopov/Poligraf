package application.liedetector.navigation

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryOwner

/**
 * Our version of ComponentContext. 
 * Bridges navigation with KMP Lifecycle, State and Instances.
 */
interface NavigationContext : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    val backPressedHandler: BackPressedHandler
}

interface BackPressedHandler {
    fun register(onBack: () -> Boolean)
    fun unregister(onBack: () -> Boolean)
    fun handleBack(): Boolean
}

class DefaultBackPressedHandler : BackPressedHandler {
    private val handlers = mutableListOf<() -> Boolean>()
    
    override fun register(onBack: () -> Boolean) { handlers.add(onBack) }
    override fun unregister(onBack: () -> Boolean) { handlers.remove(onBack) }
    
    override fun handleBack(): Boolean {
        for (handler in handlers.asReversed()) {
            if (handler()) return true
        }
        return false
    }
}

/**
 * Simple implementation of NavigationContext
 */
class DefaultNavigationContext(
    override val lifecycle: Lifecycle,
    override val viewModelStore: ViewModelStore,
    override val savedStateRegistry: SavedStateRegistry,
    override val backPressedHandler: BackPressedHandler = DefaultBackPressedHandler()
) : NavigationContext
