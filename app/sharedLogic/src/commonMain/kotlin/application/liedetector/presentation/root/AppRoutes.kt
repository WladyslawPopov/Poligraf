package application.liedetector.presentation.root

import application.liedetector.navigation.NavRoute
import kotlinx.serialization.Serializable

@Serializable
sealed class AppRoute : NavRoute {
    @Serializable
    data object Main : AppRoute()
    
    @Serializable
    data class Investigation(val subjectId: String) : AppRoute()
    
    @Serializable
    data object History : AppRoute()
    
    @Serializable
    data object Profile : AppRoute()

    @Serializable
    data object Menu : AppRoute()

    @Serializable
    data object Debug : AppRoute()
}
