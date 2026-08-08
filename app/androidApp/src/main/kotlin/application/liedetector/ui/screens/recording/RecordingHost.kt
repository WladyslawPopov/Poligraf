package application.liedetector.ui.screens.recording

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
import application.liedetector.presentation.recording.RecordingComponent
import application.liedetector.ui.components.AppScaffold
import application.liedetector.ui.components.widgets.WidgetRenderer
import application.liedetector.uicore.theme.LocalDesignSystem
import application.liedetector.uicore.theme.tokens.ColorToken
import application.liedetector.uicore.theme.tokens.IconToken
import application.liedetector.theme.utils.composeColor
import application.liedetector.ui.components.state.AppBottomSheet
import application.liedetector.uicore.theme.DesignSystem
import application.liedetector.uicore.theme.tokens.StringToken

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingHost(component: RecordingComponent) {
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
                        state.subject?.avatar?.let { avatar ->
                            Text(
                                text = avatar,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        state.subject?.name?.let { name ->
                            Text(
                                text = name,
                                color = designSystem.composeColor(ColorToken.TEXT_PRIMARY),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
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
                    border = BorderStroke(
                        1.dp,
                        designSystem.composeColor(ColorToken.GLASS_BORDER).copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RecordingActionButton(IconToken.MIC, designSystem)
                        RecordingActionButton(IconToken.GALLERY, designSystem)
                        RecordingActionButton(IconToken.NOTE, designSystem)
                    }
                }
            }
        }
        
        if (showMenu) {
            AppBottomSheet(
                onDismissRequest = { showMenu = false },
                sheetState = sheetState,
                designSystem = designSystem,
                title = designSystem.string(StringToken.DRAWER_SETTINGS)
            ) {
                ListItem(
                    headlineContent = {
                        Text(
                            designSystem.string(StringToken.ACTION_DELETE_RECORDING),
                            color = designSystem.composeColor(ColorToken.ERROR)
                        )
                    },
                    leadingContent = {
                        Icon(
                            designSystem.icon(IconToken.CLOSE),
                            contentDescription = null,
                            tint = designSystem.composeColor(ColorToken.ERROR)
                        )
                    },
                    modifier = Modifier.clickable { 
                        showMenu = false
                        component.deleteRecording() 
                    }
                )
            }
        }
    }
}

@Composable
private fun RecordingActionButton(
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
