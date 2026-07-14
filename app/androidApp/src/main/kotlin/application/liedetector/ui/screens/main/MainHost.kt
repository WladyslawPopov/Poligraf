package application.liedetector.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import application.liedetector.presentation.main.MainComponent
import application.liedetector.ui.components.widgets.WidgetRenderer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHost(component: MainComponent) {
    val state by component.viewModel.state.collectAsState()
    
    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            // Dynamic TopBar placeholder
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.error != null) {
                // Show Honest Error Message
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center).padding(32.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                    
                    items(state.widgets) { widget ->
                        WidgetRenderer(widget, onAction = { component.onAction(it) })
                    }
                }
            }
        }
    }
}
