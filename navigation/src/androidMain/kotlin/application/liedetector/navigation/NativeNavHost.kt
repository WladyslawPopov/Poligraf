package application.liedetector.navigation

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.launch

/**
 * Extension to create NavigationContext from Activity
 */
fun ComponentActivity.navigationContext(): NavigationContext {
    return DefaultNavigationContext(
        lifecycle = this.lifecycle,
        viewModelStore = this.viewModelStore,
        savedStateRegistry = this.savedStateRegistry
    )
}

/**
 * Android implementation: Creates a child LifecycleRegistry for the new screen.
 */
internal actual fun createChildContext(
    parent: NavigationContext, 
    route: NavRoute,
    id: String
): NavigationContext {
    val childLifecycle = LifecycleRegistry.createUnsafe(parent)
    
    // In a full implementation, we would use 'id' to create a scoped SavedStateRegistry
    return DefaultNavigationContext(
        lifecycle = childLifecycle,
        viewModelStore = ViewModelStore(),
        savedStateRegistry = parent.savedStateRegistry
    )
}

/**
 * Android-specific Navigation Host
 */
@Composable
fun <C : Any> NativeNavHost(
    navigator: AppNavigator<C>,
    content: @Composable (C) -> Unit
) {
    val stack by navigator.stack.collectAsState()
    val topChild = stack.lastOrNull() ?: return

    BackHandler(enabled = stack.size > 1) {
        navigator.pop()
    }

    AnimatedContent(
        targetState = topChild,
        transitionSpec = {
            (slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn())
                .togetherWith(slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) + fadeOut())
        },
        label = "NativeNavigationTransition"
    ) { target ->
        content(target.instance)
    }
}
