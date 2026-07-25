package application.liedetector.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class AppRoute {
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
