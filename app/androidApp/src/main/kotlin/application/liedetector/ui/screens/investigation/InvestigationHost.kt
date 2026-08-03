package application.liedetector.ui.screens.investigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import application.liedetector.presentation.investigation.InvestigationComponent
import application.liedetector.ui.components.AppScaffold
import application.liedetector.ui.components.widgets.WidgetRenderer
import application.liedetector.uicore.theme.LocalDesignSystem
import application.liedetector.uicore.theme.tokens.ColorToken
import application.liedetector.uicore.theme.tokens.IconToken
import application.liedetector.theme.utils.composeColor
import application.liedetector.uicore.theme.DesignSystem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestigationHost(component: InvestigationComponent) {
    val state by component.viewModel.state.collectAsState()
    val designSystem = LocalDesignSystem.current
    
    var showMenu by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    AppScaffold(
        viewModel = component.viewModel,
        state = state,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(designSystem.composeColor(ColorToken.GLASS_BASE).copy(alpha = 0.3f))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = state.subject?.avatar ?: "🕵️",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = state.subject?.name ?: "Undefined-1",
                            color = designSystem.composeColor(ColorToken.TEXT_PRIMARY),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { component.goBack() },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clip(CircleShape)
                            .background(designSystem.composeColor(ColorToken.GLASS_BASE).copy(alpha = 0.3f))
                    ) {
                        Icon(
                            imageVector = designSystem.icon(IconToken.ARROW_BACK),
                            contentDescription = null,
                            tint = designSystem.composeColor(ColorToken.TEXT_PRIMARY)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(CircleShape)
                            .background(designSystem.composeColor(ColorToken.GLASS_BASE).copy(alpha = 0.3f))
                    ) {
                        Icon(
                            imageVector = designSystem.icon(IconToken.SETTINGS),
                            contentDescription = null,
                            tint = designSystem.composeColor(ColorToken.TEXT_PRIMARY)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 1. Evidence List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.widgets.size) { index ->
                    WidgetRenderer(state.widgets[index], onAction = { })
                }
            }

            // 2. Control Panel Island
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = designSystem.composeColor(ColorToken.GLASS_BASE).copy(alpha = 0.4f),
                    modifier = Modifier.widthIn(max = 340.dp), // Not full width
                    shape = CircleShape,
                    border = BorderStroke(1.dp, designSystem.composeColor(ColorToken.GLASS_BORDER).copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        InvestigationActionButton(IconToken.MIC, designSystem)
                        InvestigationActionButton(IconToken.GALLERY, designSystem)
                        InvestigationActionButton(IconToken.NOTE, designSystem)
                    }
                }
            }
        }
        
        if (showMenu) {
            ModalBottomSheet(
                onDismissRequest = { showMenu = false },
                sheetState = sheetState,
                containerColor = designSystem.composeColor(ColorToken.SURFACE)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ListItem(
                        headlineContent = { Text("Delete Investigation", color = designSystem.composeColor(ColorToken.ERROR)) },
                        leadingContent = { Icon(designSystem.icon(IconToken.CLOSE), contentDescription = null, tint = designSystem.composeColor(ColorToken.ERROR)) },
                        modifier = Modifier.clickable { 
                            showMenu = false
                            component.deleteSubject() 
                        }
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun InvestigationActionButton(
    icon: IconToken,
    designSystem: DesignSystem
) {
    FilledIconButton(
        onClick = { },
        modifier = Modifier.size(56.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = designSystem.composeColor(ColorToken.ACCENT_PRIMARY).copy(alpha = 0.8f)
        )
    ) {
        Icon(
            imageVector = designSystem.icon(icon),
            contentDescription = null,
            tint = designSystem.composeColor(ColorToken.TEXT_INVERTED)
        )
    }
}
