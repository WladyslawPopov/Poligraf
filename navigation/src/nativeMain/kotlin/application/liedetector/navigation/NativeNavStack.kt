package application.liedetector.navigation

import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * iOS-specific wrapper for AppNavigator.
 */
class NativeNavStack<C : Any>(
    private val navigator: AppNavigator<C>,
    private val onStackChanged: (List<C>) -> Unit
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        navigator.stack
            .onEach { stack -> onStackChanged(stack.map { it.instance }) }
            .launchIn(scope)
    }

    fun push(route: NavRoute) = navigator.push(route)
    fun pop() = navigator.pop()
}

/**
 * iOS implementation of child context creation.
 */
internal actual fun createChildContext(
    parent: NavigationContext, 
    route: NavRoute,
    id: String,
    navigator: AppNavigator<Any>
): NavigationContext {
    return DefaultNavigationContext(
        lifecycle = LifecycleRegistry.createUnsafe(parent),
        viewModelStore = ViewModelStore(),
        savedStateRegistry = parent.savedStateRegistry,
        navigator = navigator
    )
}
