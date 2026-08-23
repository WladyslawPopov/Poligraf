package application.poligraf.ui.foundation.actions

sealed class NavigationAction : WidgetAction() {
    data object History : NavigationAction()
    data object Settings : NavigationAction()
}
