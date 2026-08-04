package application.liedetector.navigation

import kotlinx.serialization.Serializable

sealed class AppRoute {
    @Serializable
    data object Main : AppRoute()
    @Serializable
    data object Debug : AppRoute()
    @Serializable
    data class Investigation(val subjectId: String) : AppRoute()
}
