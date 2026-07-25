package application.liedetector.ui.screens.debug

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import application.liedetector.presentation.debug.DebugComponent
import application.liedetector.presentation.debug.DebugTab
import application.liedetector.ui.components.AppScaffold
import application.liedetector.ui.components.widgets.WidgetRenderer
import application.liedetector.uicore.theme.*
import application.liedetector.theme.utils.composeColor
import application.liedetector.uicore.types.WidgetAction
import application.liedetector.uiwidgets.models.UiWidget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugHost(component: DebugComponent) {
    val state by component.viewModel.state.collectAsState()
    val designSystem = LocalDesignSystem.current

    AppScaffold(
        viewModel = component.viewModel,
        topBar = {
            Column {

                CenterAlignedTopAppBar(
                    title = { Text(designSystem.string(StringToken.DEBUG_TITLE)) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = designSystem.composeColor(ColorToken.BACKGROUND))
                )

                SecondaryTabRow(
                    selectedTabIndex = state.selectedTab.ordinal,
                    modifier = Modifier,
                    containerColor = designSystem.composeColor(ColorToken.BACKGROUND),
                    contentColor = designSystem.composeColor(ColorToken.ACCENT_ENERGY),
                    divider = @Composable { HorizontalDivider() },
                    tabs = {
                        DebugTab.entries.forEach { tab ->
                            Tab(
                                selected = state.selectedTab == tab,
                                onClick = { component.setTab(tab) },
                                text = {
                                    val token = when (tab) {
                                        DebugTab.STATES -> StringToken.TAB_STATES
                                        DebugTab.WIDGETS -> StringToken.TAB_WIDGETS
                                        DebugTab.LABS -> StringToken.TAB_LABS
                                    }
                                    Text(designSystem.string(token))
                                }
                            )
                        }
                    }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (state.selectedTab) {
                DebugTab.STATES -> StatesTab(component)
                DebugTab.WIDGETS -> WidgetsTab(state.widgets, component)
                DebugTab.LABS -> LabsTab()
            }
        }
    }
}

@Composable
private fun StatesTab(component: DebugComponent) {
    val designSystem = LocalDesignSystem.current
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { component.onAction(WidgetAction.DEBUG_TRIGGER_LOADING) }) {
            Text(designSystem.string(StringToken.DEBUG_TRIGGER_LOADING))
        }
        Button(onClick = { component.onAction(WidgetAction.DEBUG_TRIGGER_ERROR_BLOCKING) }) {
            Text(designSystem.string(StringToken.DEBUG_TRIGGER_ERROR_BLOCKING))
        }
        Button(onClick = { component.onAction(WidgetAction.DEBUG_TRIGGER_ERROR_NON_BLOCKING) }) {
            Text(designSystem.string(StringToken.DEBUG_TRIGGER_ERROR_TOAST))
        }
        Button(onClick = { component.onAction(WidgetAction.DEBUG_TRIGGER_SUCCESS_TOAST) }) {
            Text(designSystem.string(StringToken.DEBUG_TRIGGER_SUCCESS_TOAST))
        }
    }
}

@Composable
private fun WidgetsTab(widgets: List<UiWidget>, component: DebugComponent) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(widgets) { widget ->
            WidgetRenderer(widget, onAction = { component.onAction(it) })
        }
    }
}

@Composable
private fun LabsTab() {
    val designSystem = LocalDesignSystem.current
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(designSystem.string(StringToken.LABS_EMPTY_MESSAGE), color = designSystem.composeColor(ColorToken.TEXT_SECONDARY))
    }
}
