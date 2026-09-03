package application.poligraf.ui.features.analyzer.actions

sealed class AnalyzingAction {
    data object StartNew : AnalyzingAction()
    data class Open(val subjectId: String) : AnalyzingAction()
    data object Retry : AnalyzingAction()
    data object Save : AnalyzingAction()
    data object Delete : AnalyzingAction()
}
