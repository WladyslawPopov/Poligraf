package application.liedetector.uicore.actions

sealed class NavigationAction : WidgetAction() {
    data object History : NavigationAction()
    data object Settings : NavigationAction()
    data object Profile : NavigationAction()
}
