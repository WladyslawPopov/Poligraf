package application.poligraf.ui.features.render

import androidx.compose.runtime.Composable
import application.poligraf.ui.foundation.actions.WidgetAction
import application.poligraf.ui.foundation.models.UiWidget
import application.poligraf.ui.features.recorder.AnalyzerRenderer

@Composable
fun WidgetRenderer(
    widget: UiWidget,
    onAction: (WidgetAction) -> Unit
) {
    when (widget) {
        is UiWidget.WelcomeText -> {
            WelcomeTextRenderer(widget)
        }

        is UiWidget.AnalyzeBtn -> {
            AnalyzeBtnRenderer(widget, onAction)
        }

        is UiWidget.Analyzer -> {
            AnalyzerRenderer(widget)
        }
    }
}
