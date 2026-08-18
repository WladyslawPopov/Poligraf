package application.liedetector.widgets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import application.liedetector.uicore.actions.WidgetAction
import application.liedetector.uicore.widgets.UiWidget

@Composable
fun WidgetRenderer(
    widget: UiWidget,
    onAction: (WidgetAction) -> Unit
) {
    when (widget) {
        is UiWidget.WelcomeText -> {
            WelcomeTextRenderer(widget)
        }
        
        is UiWidget.SubjectSlider -> {
            SubjectSliderRenderer(widget, onAction)
        }
        
        is UiWidget.SubjectList -> {
            SubjectListRenderer(widget, onAction)
        }
        
        else -> {
            Box(modifier = Modifier.size(0.dp))
        }
    }
}
