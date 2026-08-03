package application.liedetector.component

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryOwner

/**
 * Our version of ComponentContext. 
 * Bridges components with KMP Lifecycle, State and Instances.
 */
interface ComponentContext : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner

/**
 * Simple implementation of NavigationContext
 */
class DefaultComponentContext(
    override val lifecycle: Lifecycle,
    override val viewModelStore: ViewModelStore,
    override val savedStateRegistry: SavedStateRegistry
) : ComponentContext
