package application.poligraf.ui.features.main.models

sealed class NavigationAction {
    data object History : NavigationAction()
    data object Settings : NavigationAction()
}
