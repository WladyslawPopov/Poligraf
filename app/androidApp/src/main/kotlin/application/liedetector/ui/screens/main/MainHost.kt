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
import application.liedetector.uicore.theme.*
import application.liedetector.theme.utils.composeColor
import androidx.compose.ui.graphics.Color
import application.liedetector.navigation.AndroidNavigator
import application.liedetector.ui.screens.drawer.MainDrawer
import application.liedetector.uicore.types.WidgetAction
import androidx.activity.compose.BackHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHost(
    component: MainComponent,
    navigator: AndroidNavigator
) {
    val state by component.viewModel.state.collectAsState()
    val designSystem = LocalDesignSystem.current

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val isDrawerOpen by navigator.isDrawerOpen.collectAsState()

    // Sync drawer state from navigator
    LaunchedEffect(isDrawerOpen) {
        if (isDrawerOpen && drawerState.isClosed) {
            drawerState.open()
        } else if (!isDrawerOpen && drawerState.isOpen) {
            drawerState.close()
        }
    }

    // Sync back to navigator when drawer is closed by swipe/tap
    LaunchedEffect(drawerState.currentValue) {
        val isOpen = drawerState.currentValue == DrawerValue.Open
        if (isOpen != navigator.isDrawerOpen.value) {
            navigator.setDrawerOpen(isOpen)
        }
    }

    BackHandler(enabled = drawerState.isOpen) {
        navigator.setDrawerOpen(false)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { MainDrawer(designSystem, navigator) }
    ) {
        AppScaffold(
            viewModel = component.viewModel,
            onRetry = { component.retry() },
            onRefresh = { component.retry() },
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        val title = state.topBarState.titleToken?.let { designSystem.string(it) }
                            ?: state.topBarState.titleRaw
                            ?: ""
                        Text(
                            text = title,
                            color = designSystem.composeColor(ColorToken.TEXT_PRIMARY),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { component.onAction(WidgetAction.OPEN_SETTINGS) }) {
                            Icon(
                                imageVector = designSystem.icon(IconToken.MENU),
                                contentDescription = designSystem.string(StringToken.MENU),
                                tint = designSystem.composeColor(ColorToken.TEXT_PRIMARY)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
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
}
