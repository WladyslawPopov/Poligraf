package application.liedetector.navigation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * iOS-specific wrapper for AppNavigator to be easily used from SwiftUI.
 */
class NativeNavStack(
    private val navigator: AppNavigator,
    private val onStackChanged: (List<NavRoute>) -> Unit
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        navigator.stack
            .onEach { onStackChanged(it) }
            .launchIn(scope)
    }

    fun push(route: NavRoute) = navigator.push(route)
    fun pop() = navigator.pop()
    fun replaceAll(route: NavRoute) = navigator.replaceAll(route)
}
