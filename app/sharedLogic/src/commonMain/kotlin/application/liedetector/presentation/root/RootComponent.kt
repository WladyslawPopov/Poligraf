package application.liedetector.presentation.root

import application.liedetector.data.user.UserRepository
import application.liedetector.navigation.AppNavigator
import application.liedetector.navigation.DefaultAppNavigator
import application.liedetector.navigation.NavigationContext
import application.liedetector.presentation.main.MainComponent
import application.liedetector.presentation.main.MainViewModel
import application.liedetector.presentation.investigation.InvestigationComponent
import application.liedetector.presentation.investigation.InvestigationViewModel
import application.liedetector.engine.utils.watcher.asWatcher
import androidx.lifecycle.coroutineScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RootComponent(
    val context: NavigationContext,
    onToggleDrawer: (() -> Unit)? = null
) : KoinComponent {
    
    private val userRepository: UserRepository by inject()

    val viewModel = RootViewModel(userRepository)
    val stateWatcher = viewModel.state.asWatcher(context.lifecycle.coroutineScope)

    val navigator: AppNavigator<Any> = DefaultAppNavigator(
        startScreen = AppRoute.Main,
        rootContext = context,
        onToggleDrawer = onToggleDrawer,
        componentFactory = { route, childContext ->
            when (route) {
                is AppRoute.Main -> MainComponent(
                    childContext,
                    MainViewModel(userRepository)
                )
                is AppRoute.Investigation -> InvestigationComponent(
                    route.subjectId, 
                    childContext, 
                    InvestigationViewModel(route.subjectId)
                )
                else -> throw IllegalArgumentException("Unknown route: $route")
            }
        }
    )
}
