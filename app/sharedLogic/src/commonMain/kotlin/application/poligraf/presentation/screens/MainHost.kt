package application.poligraf.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp
import application.poligraf.presentation.main.MainComponent
import application.poligraf.widgets.WidgetRenderer
import application.poligraf.uicore.theme.tokens.ColorToken
import application.poligraf.uicore.theme.tokens.DimenToken
import application.poligraf.uicore.theme.tokens.IconToken
import application.poligraf.uicore.theme.tokens.StringToken
import application.poligraf.uicore.theme.LocalDesignSystem
import androidx.compose.ui.graphics.Color
import application.poligraf.engine.navigation.AppNavigation
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import application.poligraf.utils.BackHandler
import application.poligraf.widgets.AppScaffold
import application.poligraf.widgets.utils.AppIcon
import application.poligraf.widgets.utils.composeColor
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHost(
    component: MainComponent,
    navigator: AppNavigation
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
        drawerContent = { MainDrawer(state, designSystem, navigator) }
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
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                toolbar.titleToken?.let { token ->
                                    Text(
                                        text = designSystem.string(token),
                                        color = designSystem.composeColor(ColorToken.TEXT_PRIMARY),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                toolbar.subtitleToken?.let { token ->
                                    Text(
                                        text = designSystem.string(token),
                                        color = designSystem.composeColor(ColorToken.TEXT_SECONDARY),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = { component.onAction(toolbar.menuAction) },
                                modifier = Modifier
                                    .padding(start = designSystem.dimen(DimenToken.SPACING_SMALL).dp)
                                    .clip(CircleShape)
                                    .background(designSystem.composeColor(ColorToken.GLASS_BASE).copy(alpha = 0.3f))
                            ) {
                                AppIcon(
                                    icon = designSystem.icon(IconToken.MENU),
                                    contentDescription = designSystem.string(StringToken.MENU),
                                    tint = designSystem.composeColor(ColorToken.TEXT_PRIMARY)
                                )
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = { component.onAction(toolbar.profileAction) },
                                modifier = Modifier
                                    .padding(end = designSystem.dimen(DimenToken.SPACING_SMALL).dp)
                                    .clip(CircleShape)
                                    .background(designSystem.composeColor(ColorToken.GLASS_BASE).copy(alpha = 0.3f))
                            ) {
                                AppIcon(
                                    icon = designSystem.icon(IconToken.PROFILE),
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
