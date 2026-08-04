package application.liedetector.uicore.actions

sealed class InvestigationAction : WidgetAction() {
    data object StartNew : InvestigationAction()
    data class Open(val subjectId: String) : InvestigationAction()
    data object Retry : InvestigationAction()
}
