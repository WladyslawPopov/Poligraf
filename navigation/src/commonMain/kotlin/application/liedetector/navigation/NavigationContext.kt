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
interface NavigationContext : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner

/**
 * Simple implementation of NavigationContext
 */
class DefaultNavigationContext(
    override val lifecycle: Lifecycle,
    override val viewModelStore: ViewModelStore,
    override val savedStateRegistry: SavedStateRegistry
) : NavigationContext
