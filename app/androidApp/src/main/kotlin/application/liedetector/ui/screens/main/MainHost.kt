package application.liedetector.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import application.liedetector.presentation.main.MainComponent
import application.liedetector.ui.components.AppScaffold
import application.liedetector.ui.components.widgets.WidgetRenderer
import application.liedetector.uicore.theme.tokens.ColorToken
import application.liedetector.uicore.theme.tokens.DimenToken
import application.liedetector.uicore.theme.tokens.IconToken
import application.liedetector.uicore.theme.tokens.StringToken
import application.liedetector.uicore.theme.LocalDesignSystem
import application.liedetector.theme.utils.composeColor
import androidx.compose.ui.graphics.Color
import application.liedetector.navigation.AndroidNavigator
import application.liedetector.ui.screens.drawer.MainDrawer
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHost(
    component: MainComponent,
    navigator: AndroidNavigator
) {
    val state by component.viewModel.state.collectAsState()
    val designSystem = LocalDesignSystem.current

    var contentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200.milliseconds) // Small delay for entrance polish
        contentVisible = true
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val isDrawerOpen by navigator.isDrawerOpen.collectAsState()

    LaunchedEffect(isDrawerOpen) {
        if (isDrawerOpen && drawerState.isClosed) {
            drawerState.open()
        } else if (!isDrawerOpen && drawerState.isOpen) {
            drawerState.close()
        }
    }

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
            state = state,
            onRetry = { component.retry() },
            onRefresh = { component.retry() },
            topBar = {
                state.toolbar?.let { toolbar ->
                    CenterAlignedTopAppBar(
                        title = {
                            toolbar.titleToken?.let { token ->
                                Text(
                                    text = designSystem.string(token),
                                    color = designSystem.composeColor(ColorToken.TEXT_PRIMARY),
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { component.onAction(toolbar.menuAction) }) {
                                Icon(
                                    imageVector = designSystem.icon(IconToken.MENU),
                                    contentDescription = designSystem.string(StringToken.MENU),
                                    tint = designSystem.composeColor(ColorToken.TEXT_PRIMARY)
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { component.onAction(toolbar.profileAction) }) {
                                Icon(
                                    imageVector = designSystem.icon(IconToken.PROFILE),
                                    contentDescription = null,
                                    tint = designSystem.composeColor(ColorToken.TEXT_PRIMARY)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxHeight(),
                contentPadding = padding,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                state.welcomeWidget?.let { welcome ->
                    item {
                        WidgetRenderer(welcome, onAction = { action -> component.onAction(action) })
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = contentVisible,
                        enter = fadeIn() + slideInVertically { it / 4 }
                    ) {
                        Column {
                            state.widgets.forEach { widget ->
                                WidgetRenderer(
                                    widget = widget,
                                    onAction = { action -> component.onAction(action) }
                                )
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(designSystem.dimen(DimenToken.SPACING_LARGE).dp))
                }
            }
        }
    }
}
