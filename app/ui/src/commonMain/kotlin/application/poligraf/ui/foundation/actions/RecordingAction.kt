package application.poligraf.ui.foundation.actions

sealed class RecordingAction : WidgetAction() {
    data object StartNew : RecordingAction()
    data class Open(val subjectId: String) : RecordingAction()
    data object Retry : RecordingAction()
    data object Save : RecordingAction()
    data object Delete : RecordingAction()
}
