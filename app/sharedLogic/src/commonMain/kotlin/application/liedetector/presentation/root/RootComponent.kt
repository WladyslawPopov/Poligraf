package application.liedetector.presentation.root

import application.liedetector.data.user.UserRepository
import application.liedetector.navigation.AppNavigation
import application.liedetector.navigation.NavigationContext
import application.liedetector.presentation.main.MainComponent
import application.liedetector.presentation.main.MainViewModel
import application.liedetector.presentation.investigation.InvestigationComponent
import application.liedetector.presentation.investigation.InvestigationViewModel
import application.liedetector.presentation.debug.DebugComponent
import application.liedetector.presentation.debug.DebugViewModel
import application.liedetector.engine.utils.watcher.asWatcher
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RootComponent(
    val context: NavigationContext,
    val navigation: AppNavigation
) : KoinComponent {
    
    private val userRepository: UserRepository by inject()

    val isDrawerOpen = MutableStateFlow(false)
    val drawerOpenWatcher = isDrawerOpen.asWatcher(context.lifecycle.coroutineScope)

    val viewModel = RootViewModel(userRepository)
    val stateWatcher = viewModel.state.asWatcher(context.lifecycle.coroutineScope)

    fun toggleDrawer() {
        isDrawerOpen.value = !isDrawerOpen.value
    }

    fun setDrawerOpen(isOpen: Boolean) {
        isDrawerOpen.value = isOpen
    }
    
    // Factory methods for Native Platforms to create screen components
    
    fun createMainComponent(childContext: NavigationContext): MainComponent {
        return MainComponent(childContext, MainViewModel(userRepository, navigation))
    }
    
    fun createDebugComponent(childContext: NavigationContext): DebugComponent {
        return DebugComponent(childContext, DebugViewModel(navigation))
    }
    
    fun createInvestigationComponent(childContext: NavigationContext, subjectId: String): InvestigationComponent {
        return InvestigationComponent(subjectId, childContext, InvestigationViewModel(subjectId))
    }

    fun onDestroy() {
        (context.lifecycle as? androidx.lifecycle.LifecycleRegistry)?.currentState = androidx.lifecycle.Lifecycle.State.DESTROYED
    }
}
