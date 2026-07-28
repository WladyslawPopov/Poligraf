package application.liedetector.ui.screens.debug.tabs

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import application.liedetector.presentation.debug.DebugComponent
import application.liedetector.ui.components.widgets.WidgetRenderer
import application.liedetector.uicore.widgets.UiWidget

@Composable
fun WidgetsTab(widgets: List<UiWidget>, component: DebugComponent) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(widgets) { widget ->
            WidgetRenderer(widget, onAction = { component.onAction(it) })
        }
    }
}
