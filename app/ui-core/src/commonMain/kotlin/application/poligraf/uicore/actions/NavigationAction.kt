package application.poligraf.uicore.actions

sealed class NavigationAction : WidgetAction() {
    data object Menu : NavigationAction()
    data object History : NavigationAction()
    data object Settings : NavigationAction()
    data object Profile : NavigationAction()
}
