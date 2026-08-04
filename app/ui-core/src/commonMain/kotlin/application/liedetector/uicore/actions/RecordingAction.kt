package application.liedetector.uicore.actions

sealed class RecordingAction : WidgetAction() {
    data object Start : RecordingAction()
    data object Stop : RecordingAction()
}
