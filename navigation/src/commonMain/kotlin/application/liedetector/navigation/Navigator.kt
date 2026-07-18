package application.liedetector.navigation

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Marker interface for screen destinations. 
 * Real routes should be @Serializable.
 */
interface NavRoute

/**
 * A wrapper that holds an instantiated component and its navigation context.
 */
data class Child<out C : Any>(
    val id: String,
    val route: NavRoute,
    val instance: C,
    val context: NavigationContext
)

/**
 * Core navigation interface.
 */
interface AppNavigator<C : Any> {
    val stack: StateFlow<List<Child<C>>>
    
    fun push(route: NavRoute)
    
    /**
     * Push a new route and register a callback for the result when it pops.
     */
    fun <R : Any> pushForResult(route: NavRoute, onResult: (R) -> Unit)
    
    /**
     * Pop current screen. Optional result can be passed back.
     */
    fun pop(result: Any? = null)
    
    /**
     * Clear stack and set new root.
     */
    fun replaceAll(route: NavRoute)

    /**
     * Pop everything except the first screen.
     */
    fun popToRoot()

    /**
     * Toggle global drawer/menu if applicable.
     */
    fun toggleDrawer()

    /**
     * Explicitly set drawer state.
     */
    fun setDrawerOpen(isOpen: Boolean)

    /**
     * Events for triggering drawer toggle in UI.
     */
    val isDrawerOpen: StateFlow<Boolean>

    /**
     * State restoration: Save current stack as a list of serialized strings.
     */
    fun saveState(serializer: (NavRoute) -> String): List<String>

    /**
     * State restoration: Restore stack from serialized strings.
     */
    fun restoreState(state: List<String>, deserializer: (String) -> NavRoute)
}

/**
 * Base implementation with Lifecycle, Results and State management.
 */
class DefaultAppNavigator<C : Any>(
    startScreen: NavRoute,
    private val rootContext: NavigationContext,
    private val componentFactory: (NavRoute, NavigationContext) -> C,
    private val stackKey: String = "default_stack"
) : AppNavigator<C> {

    private val _stack = MutableStateFlow<List<Child<C>>>(emptyList())
    override val stack: StateFlow<List<Child<C>>> = _stack.asStateFlow()

    private val _isDrawerOpen = MutableStateFlow(false)
    override val isDrawerOpen: StateFlow<Boolean> = _isDrawerOpen.asStateFlow()

    private val resultCallbacks = mutableMapOf<String, (Any) -> Unit>()

    init {
        // Only push start screen if stack is empty (might be empty during manual restore)
        if (_stack.value.isEmpty()) {
            push(startScreen)
        }
    }

    override fun toggleDrawer() {
        _isDrawerOpen.value = !_isDrawerOpen.value
    }

    override fun setDrawerOpen(isOpen: Boolean) {
        _isDrawerOpen.value = isOpen
    }

    override fun push(route: NavRoute) {
        internalPush(route, null)
    }

    override fun <R : Any> pushForResult(route: NavRoute, onResult: (R) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        internalPush(route, onResult as (Any) -> Unit)
    }

    private fun internalPush(route: NavRoute, onResult: ((Any) -> Unit)?) {
        val currentStack = _stack.value
        
        currentStack.lastOrNull()?.let { last ->
            (last.context.lifecycle as? LifecycleRegistry)?.currentState = Lifecycle.State.STARTED
        }

        // Generate a truly unique ID for this instance in the stack
        val childId = "${stackKey}_${route::class.simpleName}_${_stack.value.size}_${hashCode()}"
        
        @Suppress("UNCHECKED_CAST")
        val childContext = createChildContext(rootContext, route, childId, this as AppNavigator<Any>)
        val instance = componentFactory(route, childContext)
        
        (childContext.lifecycle as? LifecycleRegistry)?.currentState = Lifecycle.State.RESUMED
        
        val newChild = Child(childId, route, instance, childContext)
        
        if (onResult != null) {
            resultCallbacks[newChild.id] = onResult
        }

        _stack.value += newChild
    }

    override fun pop(result: Any?) {
        val currentStack = _stack.value
        if (currentStack.isNotEmpty()) {
            val topChild = currentStack.last()
            
            // Check if component wants to handle back press itself
            if (topChild.context.backPressedHandler.handleBack()) {
                return
            }

            if (currentStack.size > 1) {
                val poppedChild = currentStack.last()
                val newStack = currentStack.dropLast(1)
                
                (poppedChild.context.lifecycle as? LifecycleRegistry)?.currentState = Lifecycle.State.DESTROYED
                poppedChild.context.viewModelStore.clear()
                
                if (result != null) {
                    resultCallbacks[poppedChild.id]?.invoke(result)
                }
                resultCallbacks.remove(poppedChild.id)
                
                newStack.lastOrNull()?.let { top ->
                    (top.context.lifecycle as? LifecycleRegistry)?.currentState = Lifecycle.State.RESUMED
                }
                
                _stack.value = newStack
            }
        }
    }

    override fun popToRoot() {
        val currentStack = _stack.value
        if (currentStack.size <= 1) return
        
        val root = currentStack.first()
        val toDelete = currentStack.drop(1)
        
        toDelete.forEach { 
            (it.context.lifecycle as? LifecycleRegistry)?.currentState = Lifecycle.State.DESTROYED
            it.context.viewModelStore.clear() 
        }
        
        (root.context.lifecycle as? LifecycleRegistry)?.currentState = Lifecycle.State.RESUMED
        _stack.value = listOf(root)
    }

    override fun replaceAll(route: NavRoute) {
        val oldStack = _stack.value
        _stack.value = emptyList()
        resultCallbacks.clear()
        
        oldStack.forEach { 
            (it.context.lifecycle as? LifecycleRegistry)?.currentState = Lifecycle.State.DESTROYED
            it.context.viewModelStore.clear() 
        }

        push(route)
    }

    override fun saveState(serializer: (NavRoute) -> String): List<String> {
        return _stack.value.map { serializer(it.route) }
    }

    override fun restoreState(state: List<String>, deserializer: (String) -> NavRoute) {
        val oldStack = _stack.value
        _stack.value = emptyList()
        oldStack.forEach { 
            (it.context.lifecycle as? LifecycleRegistry)?.currentState = Lifecycle.State.DESTROYED
            it.context.viewModelStore.clear() 
        }

        state.forEach { serializedRoute ->
            push(deserializer(serializedRoute))
        }
    }
}

/**
 * Navigator that manages multiple stacks (e.g. for Bottom Navigation).
 */
class MultiStackAppNavigator<C : Any>(
    initialRoutes: Map<String, NavRoute>,
    private val rootContext: NavigationContext,
    private val componentFactory: (NavRoute, NavigationContext) -> C
) {
    private val _navigators = initialRoutes.mapValues { (key, route) ->
        DefaultAppNavigator(route, rootContext, componentFactory, stackKey = key)
    }

    private val _activeKey = MutableStateFlow(initialRoutes.keys.first())
    val activeKey: StateFlow<String> = _activeKey.asStateFlow()

    /**
     * Get navigator for the specific tab/stack.
     */
    fun getNavigator(key: String): AppNavigator<C> {
        return _navigators[key] ?: throw IllegalArgumentException("No stack for key: $key")
    }

    /**
     * Switch to another stack and handle lifecycles.
     */
    fun switch(key: String) {
        if (_activeKey.value == key) return
        
        // PAUSE current active stack
        val currentNav = getNavigator(_activeKey.value)
        currentNav.stack.value.lastOrNull()?.let { 
            (it.context.lifecycle as? LifecycleRegistry)?.currentState = Lifecycle.State.STARTED 
        }

        _activeKey.value = key

        // RESUME new active stack
        val newNav = getNavigator(key)
        newNav.stack.value.lastOrNull()?.let { 
            (it.context.lifecycle as? LifecycleRegistry)?.currentState = Lifecycle.State.RESUMED 
        }
    }

    /**
     * State restoration for Multi-stack.
     * Returns a map of Stack Key -> List of Serialized Routes.
     */
    fun saveState(serializer: (NavRoute) -> String): Map<String, List<String>> {
        return _navigators.mapValues { it.value.saveState(serializer) }
    }

    /**
     * Restore all stacks from saved state.
     */
    fun restoreState(state: Map<String, List<String>>, deserializer: (String) -> NavRoute) {
        state.forEach { (key, stackState) ->
            _navigators[key]?.restoreState(stackState, deserializer)
        }
    }
}

internal expect fun createChildContext(
    parent: NavigationContext, 
    route: NavRoute, 
    id: String,
    navigator: AppNavigator<Any>
): NavigationContext
