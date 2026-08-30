package application.poligraf.ui.foundation.actions

sealed class NavigationAction {
    data object History : NavigationAction()
    data object Settings : NavigationAction()
}
