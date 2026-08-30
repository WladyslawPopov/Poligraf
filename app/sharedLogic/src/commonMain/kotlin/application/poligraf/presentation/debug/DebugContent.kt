package application.poligraf.presentation.debug

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import application.poligraf.ui.components.layout.AppScaffold
import application.poligraf.ui.components.layout.GlassSegmentedTabRow
import application.poligraf.presentation.debug.ui.tabs.LabsTab
import application.poligraf.presentation.debug.ui.tabs.StatesTab
import application.poligraf.ui.components.icons.AppIcon
import application.poligraf.presentation.debug.data.DebugTab
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.IconToken
import application.poligraf.ui.theme.tokens.StringToken
import com.arkivanov.decompose.extensions.compose.subscribeAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugContent(component: DebugComponent) {
    val componentModel by component.model.subscribeAsState()
    val viewModel = componentModel.viewModel
    val state by viewModel.state.collectAsState()

    val designSystem = LocalDesignSystem.current

    val pagerState = rememberPagerState(
        initialPage = state.selectedTab.ordinal,
        pageCount = { DebugTab.entries.size }
    )

    LaunchedEffect(pagerState.settledPage) {
        viewModel.setTab(DebugTab.entries[pagerState.settledPage])
    }

    LaunchedEffect(state.selectedTab) {
        if (pagerState.currentPage != state.selectedTab.ordinal) {
            pagerState.animateScrollToPage(state.selectedTab.ordinal)
        }
    }

    AppScaffold(
        viewModel = viewModel,
        state = state,
        topBar = {
            Column(modifier = Modifier.statusBarsPadding()) {
                CenterAlignedTopAppBar(
                    title = { Text(designSystem.string(StringToken.DEBUG_TITLE)) },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.goBack() }) {
                            AppIcon(
                                icon = designSystem.icon(IconToken.ARROW_BACK),
                                contentDescription = null,
                                tint = designSystem.color(ColorToken.TEXT_PRIMARY)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )

                GlassSegmentedTabRow(
                    items = DebugTab.entries.toTypedArray(),
                    selectedIndex = state.selectedTab.ordinal,
                    onTabSelected = { viewModel.setTab(it) },
                    labelProvider = { tab ->
                        when (tab) {
                            DebugTab.STATES -> "States"
                            DebugTab.LABS -> "Labs"
                        }
                    }
                )
            }
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Top
        ) { page ->
            when (DebugTab.entries[page]) {
                DebugTab.STATES -> StatesTab(viewModel, padding)
                DebugTab.LABS -> LabsTab()
            }
        }
    }
}
