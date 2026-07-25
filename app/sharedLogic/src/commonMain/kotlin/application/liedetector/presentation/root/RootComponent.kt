package application.liedetector.presentation.root

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import application.liedetector.data.user.UserRepository
import application.liedetector.navigation.AppNavigation
import application.liedetector.presentation.main.MainComponent
import application.liedetector.presentation.main.MainViewModel
import application.liedetector.presentation.investigation.InvestigationComponent
import application.liedetector.presentation.investigation.InvestigationViewModel
import application.liedetector.presentation.debug.DebugComponent
import application.liedetector.presentation.debug.DebugViewModel
import application.liedetector.engine.utils.watcher.asWatcher
import androidx.lifecycle.coroutineScope
import application.liedetector.engine.component.ComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RootComponent(
    val context: ComponentContext,
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
    
    fun createMainComponent(): MainComponent {
        return MainComponent(context, MainViewModel(userRepository, navigation))
    }
    
    fun createDebugComponent(): DebugComponent {
        return DebugComponent(context, DebugViewModel(navigation))
    }
    
    fun createInvestigationComponent(subjectId: String): InvestigationComponent {
        return InvestigationComponent(subjectId, context, InvestigationViewModel(subjectId))
    }

    fun onDestroy() {
        (context.lifecycle as? LifecycleRegistry)?.currentState = Lifecycle.State.DESTROYED
    }
}
