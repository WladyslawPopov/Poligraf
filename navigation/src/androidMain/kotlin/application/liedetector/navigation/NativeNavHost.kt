package application.liedetector.navigation

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.launch

/**
 * Extension to create NavigationContext from Activity
 */
fun ComponentActivity.navigationContext(navigator: AppNavigator<Any>? = null): NavigationContext {
    return DefaultNavigationContext(
        lifecycle = this.lifecycle,
        viewModelStore = this.viewModelStore,
        savedStateRegistry = this.savedStateRegistry,
        navigator = navigator
    )
}

/**
 * Android implementation: Creates a child LifecycleRegistry for the new screen.
 */
internal actual fun createChildContext(
    parent: NavigationContext, 
    route: NavRoute,
    id: String,
    navigator: AppNavigator<Any>
): NavigationContext {
    val childLifecycle = LifecycleRegistry.createUnsafe(parent)
    
    // In a full implementation, we would use 'id' to create a scoped SavedStateRegistry
    return DefaultNavigationContext(
        lifecycle = childLifecycle,
        viewModelStore = ViewModelStore(),
        savedStateRegistry = parent.savedStateRegistry,
        navigator = navigator
    )
}

/**
 * Android-specific Navigation Host
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <C : Any> NativeNavHost(
    navigator: AppNavigator<C>,
    drawerState: DrawerState? = null,
    drawerContent: @Composable (() -> Unit)? = null,
    content: @Composable (C) -> Unit
) {
    val stack by navigator.stack.collectAsState()
    val topChild = stack.lastOrNull() ?: return
    val scope = rememberCoroutineScope()

    BackHandler(enabled = stack.size > 1 || (drawerState?.isOpen == true)) {
        if (drawerState?.isOpen == true) {
            scope.launch { drawerState.close() }
        } else {
            navigator.pop()
        }
    }

    // Navigation & Drawer Layer
    if (drawerContent != null && drawerState != null) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = drawerContent,
            modifier = Modifier.fillMaxSize()
        ) {
            NavigationContent(topChild, content)
        }
    } else {
        NavigationContent(topChild, content)
    }
}

@Composable
private fun <C : Any> NavigationContent(
    topChild: Child<C>,
    content: @Composable (C) -> Unit
) {
    AnimatedContent(
        targetState = topChild,
        transitionSpec = {
            (slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn())
                .togetherWith(slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) + fadeOut())
        },
        label = "NativeNavigationTransition",
        modifier = Modifier.fillMaxSize()
    ) { target ->
        content(target.instance)
    }
}
