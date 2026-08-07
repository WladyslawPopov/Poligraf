package application.liedetector.uicore.actions

sealed class RecordingAction : WidgetAction() {
    data object StartNew : RecordingAction()
    data class Open(val subjectId: String) : RecordingAction()
    data object Retry : RecordingAction()
}
