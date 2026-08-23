package application.poligraf.ui.foundation.actions

sealed class WidgetAction {
    data class ToggleSelection(val id: String) : WidgetAction()
    
    object DeleteSelected : WidgetAction()
    
    object ClearSelection : WidgetAction()
}
