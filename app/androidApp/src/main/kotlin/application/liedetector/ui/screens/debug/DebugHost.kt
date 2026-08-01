package application.liedetector.ui.screens.debug

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import application.liedetector.presentation.debug.DebugComponent
import application.liedetector.presentation.debug.data.DebugTab
import application.liedetector.ui.components.AppScaffold
import application.liedetector.ui.components.GlassSegmentedTabRow
import application.liedetector.ui.screens.debug.tabs.LabsTab
import application.liedetector.ui.screens.debug.tabs.StatesTab
import application.liedetector.ui.screens.debug.tabs.WidgetsTab
import application.liedetector.uicore.theme.*
import application.liedetector.theme.utils.composeColor
import application.liedetector.uicore.theme.tokens.ColorToken
import application.liedetector.uicore.theme.tokens.IconToken
import application.liedetector.uicore.theme.tokens.StringToken

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugHost(component: DebugComponent) {
    val state by component.viewModel.state.collectAsState()
    val designSystem = LocalDesignSystem.current

    val pagerState = rememberPagerState(
        initialPage = state.selectedTab.ordinal,
        pageCount = { DebugTab.entries.size }
    )

    // Sync Pager -> ViewModel
    LaunchedEffect(pagerState.currentPage) {
        component.setTab(DebugTab.entries[pagerState.currentPage])
    }

    // Sync ViewModel -> Pager
    LaunchedEffect(state.selectedTab) {
        if (pagerState.currentPage != state.selectedTab.ordinal) {
            pagerState.animateScrollToPage(state.selectedTab.ordinal)
        }
    }

    AppScaffold(
        viewModel = component.viewModel,
        state = state,
        topBar = {
            Column(modifier = Modifier.statusBarsPadding()) {
                CenterAlignedTopAppBar(
                    title = { Text(designSystem.string(StringToken.DEBUG_TITLE)) },
                    navigationIcon = {
                        IconButton(onClick = { component.goBack() }) {
                            Icon(
                                imageVector = designSystem.icon(IconToken.ARROW_BACK),
                                contentDescription = null,
                                tint = designSystem.composeColor(ColorToken.TEXT_PRIMARY)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )

                GlassSegmentedTabRow(
                    items = DebugTab.entries.toTypedArray(),
                    selectedIndex = state.selectedTab.ordinal,
                    onTabSelected = { component.setTab(it) },
                    labelProvider = { tab ->
                        val token = when (tab) {
                            DebugTab.STATES -> StringToken.TAB_STATES
                            DebugTab.WIDGETS -> StringToken.TAB_WIDGETS
                            DebugTab.LABS -> StringToken.TAB_LABS
                        }
                        designSystem.string(token)
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
                DebugTab.STATES -> StatesTab(component, padding)
                DebugTab.WIDGETS -> WidgetsTab(state.widgets, component, padding)
                DebugTab.LABS -> LabsTab()
            }
        }
    }
}
