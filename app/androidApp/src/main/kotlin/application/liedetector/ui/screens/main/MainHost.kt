package application.liedetector.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import application.liedetector.presentation.main.MainComponent
import application.liedetector.ui.components.AppScaffold
import application.liedetector.ui.components.widgets.WidgetRenderer
import application.liedetector.uicore.theme.LocalDesignSystem
import application.liedetector.uicore.theme.ColorToken
import application.liedetector.uicore.theme.DimenToken
import application.liedetector.theme.utils.composeColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHost(component: MainComponent) {
    val state by component.viewModel.state.collectAsState()
    val designSystem = LocalDesignSystem.current
    
    AppScaffold(
        viewModel = component.viewModel,
        onRetry = { component.retry() },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = state.topBarState.title,
                        color = designSystem.composeColor(ColorToken.TEXT_PRIMARY),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { component.context.navigator?.toggleDrawer() }) {
                        Icon(
                            imageVector = Icons.Rounded.Menu,
                            contentDescription = "Menu",
                            tint = designSystem.composeColor(ColorToken.TEXT_PRIMARY)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { 
                Spacer(modifier = Modifier.height(designSystem.dimen(DimenToken.SPACING_LARGE).dp)) 
            }
            
            items(state.widgets) { widget ->
                WidgetRenderer(widget, onAction = { component.onAction(it) })
            }
        }
    }
}
