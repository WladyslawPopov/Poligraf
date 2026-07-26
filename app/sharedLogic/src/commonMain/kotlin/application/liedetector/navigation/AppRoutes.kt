package application.liedetector.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class AppRoute {
    @Serializable
    data object Main : AppRoute()
    @Serializable
    data object Debug : AppRoute()
}
