package application.liedetector.presentation.screens.debug.tabs

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import application.liedetector.presentation.debug.DebugComponent
import application.liedetector.widgets.WidgetRenderer
import application.liedetector.uicore.widgets.UiWidget

@Composable
fun WidgetsTab(widgets: List<UiWidget>, component: DebugComponent, padding: PaddingValues) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = padding
    ) {
        items(widgets) { widget ->
            WidgetRenderer(widget, onAction = { component.onAction(it) })
        }
    }
}
