package application.poligraf.ui.features.render

import androidx.compose.runtime.Composable
import application.poligraf.ui.foundation.actions.WidgetAction
import application.poligraf.ui.foundation.models.UiWidget

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
    }
}
