package application.poligraf.presentation.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import application.poligraf.uicore.theme.LocalDesignSystem
import application.poligraf.uicore.theme.tokens.ColorToken
import application.poligraf.uicore.theme.tokens.DimenToken
import application.poligraf.uicore.theme.tokens.IconToken
import application.poligraf.widgets.AppScaffold
import application.poligraf.widgets.WidgetRenderer
import application.poligraf.widgets.utils.AppIcon
import application.poligraf.widgets.utils.composeColor
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    component: MainComponent
) {
    val componentModel by component.model.subscribeAsState()
    val viewModel = componentModel.viewModel
    val state by viewModel.state.collectAsState()

    val designSystem = LocalDesignSystem.current

    var contentVisible by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    LaunchedEffect(Unit) {
        delay(200.milliseconds) // Small delay for entrance polish
        contentVisible = true
    }

    // Sync ViewModel State -> DrawerState
    LaunchedEffect(state.isDrawerOpen) {
        if (state.isDrawerOpen && drawerState.isClosed) {
            drawerState.open()
        } else if (!state.isDrawerOpen && drawerState.isOpen) {
            drawerState.close()
        }
    }

    // Sync DrawerState -> ViewModel State (e.g. user swiped to close)
    LaunchedEffect(drawerState.currentValue) {
        val isOpen = drawerState.currentValue == DrawerValue.Open
        if (isOpen != state.isDrawerOpen) {
            viewModel.setDrawerOpen(isOpen)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            MainDrawer(
                state = state,
                designSystem = designSystem,
                viewModel = viewModel
            )
        }
    ) {
        AppScaffold(
            viewModel = viewModel,
            state = state,
            onRetry = { viewModel.loadContent() },
            onRefresh = { viewModel.loadContent() },
            topBar = {
                state.toolbar?.let { toolbar ->
                    CenterAlignedTopAppBar(
                        title = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                toolbar.titleProvider?.let { provider ->
                                    Text(
                                        text = provider(designSystem.strings),
                                        color = designSystem.composeColor(ColorToken.TEXT_PRIMARY),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                toolbar.subtitleProvider?.let { provider ->
                                    Text(
                                        text = provider(designSystem.strings),
                                        color = designSystem.composeColor(ColorToken.TEXT_SECONDARY),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = { viewModel.onWidgetAction(toolbar.menuAction) },
                                modifier = Modifier
                                    .padding(start = designSystem.dimen(DimenToken.SPACING_SMALL).dp)
                                    .clip(CircleShape)
                                    .background(designSystem.composeColor(ColorToken.GLASS_BASE).copy(alpha = 0.3f))
                            ) {
                                AppIcon(
                                    icon = designSystem.icon(IconToken.MENU),
                                    contentDescription = designSystem.strings.common.menu,
                                    tint = designSystem.composeColor(ColorToken.TEXT_PRIMARY)
                                )
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = { viewModel.onWidgetAction(toolbar.profileAction) },
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
                        WidgetRenderer(welcome, onAction = { action -> viewModel.onWidgetAction(action) })
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
                                    onAction = { action -> viewModel.onWidgetAction(action) }
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
