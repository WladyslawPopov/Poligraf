package application.liedetector.navigation

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*

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
 * Android-specific Navigation Host that renders the top screen from the AppNavigator stack.
 */
@Composable
fun NativeNavHost(
    navigator: AppNavigator,
    content: @Composable (NavRoute) -> Unit
) {
    val stack by navigator.stack.collectAsState()
    val topRoute = stack.last()

    // Handle system back button
    BackHandler(enabled = stack.size > 1) {
        navigator.pop()
    }

    AnimatedContent(
        targetState = topRoute,
        transitionSpec = {
            (slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn())
                .togetherWith(slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) + fadeOut())
        },
        label = "NativeNavigationTransition"
    ) { targetRoute ->
        content(targetRoute)
    }
}
