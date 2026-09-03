package application.poligraf.ui.features.history.actions

sealed class HistoryAction {
    data class ToggleSelection(val id: String) : HistoryAction()
    data object DeleteSelected : HistoryAction()
    data object ClearSelection : HistoryAction()
}
