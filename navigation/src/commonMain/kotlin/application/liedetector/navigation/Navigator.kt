package application.liedetector.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Marker interface for all screen destinations.
 * Renamed to NavRoute to avoid conflicts with system libraries.
 */
interface NavRoute

/**
 * Core navigation interface to be used in Shared Logic (ViewModels)
 */
interface AppNavigator {
    val stack: StateFlow<List<NavRoute>>
    
    fun push(route: NavRoute)
    fun pop()
    fun replaceAll(route: NavRoute)
    
    // For state restoration
    fun saveState(): List<String> 
    fun restoreState(state: List<String>, routeFactory: (String) -> NavRoute)
}

/**
 * Implementation of AppNavigator that uses StateFlow to notify platform-specific NavHosts
 */
class DefaultAppNavigator(startScreen: NavRoute) : AppNavigator {
    private val _stack = MutableStateFlow(listOf(startScreen))
    override val stack: StateFlow<List<NavRoute>> = _stack.asStateFlow()

    override fun push(route: NavRoute) {
        _stack.value = _stack.value + route
    }

    override fun pop() {
        if (_stack.value.size > 1) {
            _stack.value = _stack.value.dropLast(1)
        }
    }

    override fun replaceAll(route: NavRoute) {
        _stack.value = listOf(route)
    }

    override fun saveState(): List<String> {
        return _stack.value.map { it::class.simpleName ?: "" }
    }

    override fun restoreState(state: List<String>, routeFactory: (String) -> NavRoute) {
        if (state.isNotEmpty()) {
            _stack.value = state.map { routeFactory(it) }
        }
    }
}
