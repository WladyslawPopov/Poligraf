package application.liedetector.engine.component

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryOwner
import kotlin.reflect.KClass

/**
 * Our version of ComponentContext. 
 * Bridges components with KMP Lifecycle, State and Instances.
 */
interface ComponentContext : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    val instanceKeeper: InstanceKeeper
    fun childContext(key: String): ComponentContext
}

/**
 * Manages instances that should be retained across configuration changes.
 */
interface InstanceKeeper {
    fun <T : Any> getOrCreate(key: String, factory: () -> T): T
}

/**
 * Simple implementation of NavigationContext
 */
class DefaultComponentContext(
    override val lifecycle: Lifecycle,
    override val viewModelStore: ViewModelStore,
    override val savedStateRegistry: SavedStateRegistry
) : ComponentContext {

    override val instanceKeeper: InstanceKeeper by lazy {
        DefaultInstanceKeeper(viewModelStore)
    }

    override fun childContext(key: String): ComponentContext {
        // By default, a child context can share the same store or create a sub-store.
        // For simplicity and to follow the user's request for "native" management,
        // we share the store but use unique keys for instances.
        return DefaultComponentContext(lifecycle, viewModelStore, savedStateRegistry)
    }
}

private class DefaultInstanceKeeper(
    private val viewModelStore: ViewModelStore
) : InstanceKeeper {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getOrCreate(key: String, factory: () -> T): T {
        val container = ViewModelProvider.create(
            viewModelStore,
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
                    return InstanceContainer() as T
                }
            }
        )[InstanceContainer::class]
        
        return container.instances.getOrPut(key) { factory() } as T
    }
}

internal class InstanceContainer : ViewModel() {
    val instances = mutableMapOf<String, Any>()
    
    override fun onCleared() {
        instances.values.forEach { 
            if (it is Disposable) it.dispose()
        }
        instances.clear()
        super.onCleared()
    }
}

/**
 * Optional interface for components that need manual cleanup.
 */
interface Disposable {
    fun dispose()
}
