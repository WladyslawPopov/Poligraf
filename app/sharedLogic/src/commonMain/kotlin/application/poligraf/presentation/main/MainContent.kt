package application.poligraf.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import application.poligraf.presentation.main.ui.MainDrawer
import application.poligraf.uicore.theme.LocalDesignSystem
import application.poligraf.uicore.theme.tokens.ColorToken
import application.poligraf.uicore.theme.tokens.DimenToken
import application.poligraf.uicore.theme.tokens.IconToken
import application.poligraf.widgets.AppScaffold
import application.poligraf.widgets.AppIcon
import com.arkivanov.decompose.extensions.compose.subscribeAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    component: MainComponent
) {
    val componentModel by component.model.subscribeAsState()
    val viewModel = componentModel.viewModel
    val state by viewModel.state.collectAsState()

    val designSystem = LocalDesignSystem.current

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

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
                                        color = designSystem.color(ColorToken.TEXT_PRIMARY),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                toolbar.subtitleProvider?.let { provider ->
                                    Text(
                                        text = provider(designSystem.strings),
                                        color = designSystem.color(ColorToken.TEXT_SECONDARY),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = { viewModel.onWidgetAction(toolbar.menuAction) },
                                modifier = Modifier
                                    .padding(start = designSystem.dimen(DimenToken.SPACING_SMALL))
                                    .clip(CircleShape)
                                    .background(
                                        designSystem.color(ColorToken.GLASS_BASE)
                                            .copy(alpha = 0.3f)
                                    )
                            ) {
                                AppIcon(
                                    icon = designSystem.icon(IconToken.MENU),
                                    contentDescription = designSystem.strings.common.menu,
                                    tint = designSystem.color(ColorToken.TEXT_PRIMARY)
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

        }
    }
}
