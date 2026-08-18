package application.poligraf.presentation.debug.ui.tabs

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import application.poligraf.widgets.WidgetRenderer
import application.poligraf.presentation.debug.DebugViewModel
import application.poligraf.uicore.widgets.UiWidget

@Composable
fun WidgetsTab(widgets: List<UiWidget>, viewModel: DebugViewModel, padding: PaddingValues) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = padding
    ) {
        items(widgets) { widget ->
            WidgetRenderer(widget, onAction = { viewModel.onWidgetAction(it) })
        }
    }
}
