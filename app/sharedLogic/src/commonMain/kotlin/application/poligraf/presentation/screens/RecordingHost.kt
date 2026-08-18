package application.poligraf.presentation.screens

import androidx.compose.animation.*
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
import application.liedetector.widgets.AppScaffold
import application.liedetector.widgets.WidgetRenderer
import application.liedetector.uicore.theme.LocalDesignSystem
import application.liedetector.uicore.theme.tokens.ColorToken
import application.liedetector.uicore.theme.tokens.IconToken
import application.liedetector.widgets.utils.composeColor
import application.liedetector.widgets.utils.AppIcon
import application.liedetector.widgets.state.AppBottomSheet
import application.liedetector.uicore.theme.tokens.StringToken
import application.liedetector.uicore.state.VoiceRecorderUiState
import application.liedetector.uicore.state.VoiceRecorderAction
import application.liedetector.presentation.recording.data.MaterialTag
import application.liedetector.uicore.theme.DesignSystem
import application.liedetector.uicore.theme.tokens.DimenToken

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingHost(component: RecordingComponent) {
    val viewModel = component.viewModel
    val state by viewModel.state.collectAsState()
    val designSystem = LocalDesignSystem.current
    
    val widgets = remember(state.widgets) { state.widgets }

    var showMenu by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    AppScaffold(
        viewModel = viewModel,
        state = state,
        topBar = {
            val subject = remember(state.subject) { state.subject }
            RecordingTopBar(
                avatar = subject.avatar,
                name = subject.name,
                onBack = { component.goBack() },
                onMenu = { showMenu = true }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding() + designSystem.dimen(DimenToken.SPACING_MEDIUM).dp,
                    bottom = padding.calculateBottomPadding() + 160.dp,
                    start = designSystem.dimen(DimenToken.MAIN_PADDING).dp,
                    end = designSystem.dimen(DimenToken.MAIN_PADDING).dp
                ),
                verticalArrangement = Arrangement.spacedBy(designSystem.dimen(DimenToken.WIDGET_SPACING).dp)
            ) {
                item {
                    MaterialsHeader(state.materials) { component.onMaterialTagClicked(it) }
                }

                items(
                    count = widgets.size,
                    key = { index -> widgets[index].id }
                ) { index ->
                    WidgetRenderer(
                        widget = widgets[index],
                        onAction = { }
                    )
                }
            }

            BottomControlPanel(
                onMicClick = {
                    // Assuming permissions are handled by native host or already granted
                    viewModel.onMicClicked()
                },
                onGalleryClick = { viewModel.onGalleryClicked() },
                onNoteClick = { viewModel.onNoteClicked() }
            )
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
                        AppIcon(
                            icon = designSystem.icon(IconToken.CLOSE),
                            contentDescription = null,
                            tint = designSystem.composeColor(ColorToken.ERROR)
                        )
                    },
                    modifier = Modifier.clickable { 
                        showMenu = false
                        viewModel.deleteRecording() 
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordingTopBar(
    avatar: String,
    name: String,
    onBack: () -> Unit,
    onMenu: () -> Unit
) {
    val designSystem = LocalDesignSystem.current
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
                    text = avatar,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = name,
                    color = designSystem.composeColor(ColorToken.TEXT_PRIMARY),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clip(CircleShape)
                    .background(designSystem.composeColor(ColorToken.GLASS_BASE).copy(alpha = 0.3f))
            ) {
                AppIcon(
                    icon = designSystem.icon(IconToken.ARROW_BACK),
                    contentDescription = null,
                    tint = designSystem.composeColor(ColorToken.TEXT_PRIMARY)
                )
            }
        },
        actions = {
            IconButton(
                onClick = onMenu,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .clip(CircleShape)
                    .background(designSystem.composeColor(ColorToken.GLASS_BASE).copy(alpha = 0.3f))
            ) {
                AppIcon(
                    icon = designSystem.icon(IconToken.SETTINGS),
                    contentDescription = null,
                    tint = designSystem.composeColor(ColorToken.TEXT_PRIMARY)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

@Composable
private fun BoxScope.BottomControlPanel(
    onMicClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onNoteClick: () -> Unit
) {
    val designSystem = LocalDesignSystem.current

    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .padding(horizontal = 0.dp)
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = designSystem.composeColor(ColorToken.GLASS_BASE).copy(alpha = 0.4f),
            modifier = Modifier.widthIn(max = designSystem.dimen(DimenToken.DRAWER_WIDTH).dp + 20.dp),
            shape = CircleShape,
            border = BorderStroke(
                width = designSystem.dimen(DimenToken.DIVIDER_THICKNESS).dp,
                color = designSystem.composeColor(ColorToken.GLASS_BORDER).copy(alpha = 0.1f)
            )
        ) {
            Row(
                modifier = Modifier.padding(designSystem.dimen(DimenToken.SPACING_SMALL).dp),
                horizontalArrangement = Arrangement.spacedBy(designSystem.dimen(DimenToken.SPACING_LARGE).dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RecordingActionButton(IconToken.MIC, designSystem, onMicClick)
                RecordingActionButton(IconToken.GALLERY, designSystem, onGalleryClick)
                RecordingActionButton(IconToken.NOTE, designSystem, onNoteClick)
            }
        }
    }
}

@Composable
private fun MaterialsHeader(
    materials: List<MaterialTag>,
    onTagClick: (String) -> Unit
) {
    val designSystem = LocalDesignSystem.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = designSystem.dimen(DimenToken.MAIN_PADDING).dp, 
                vertical = designSystem.dimen(DimenToken.SPACING_SMALL).dp + 4.dp
            ),
        horizontalArrangement = Arrangement.spacedBy(designSystem.dimen(DimenToken.SPACING_SMALL).dp)
    ) {
        materials.forEach { tag ->
            AssistChip(
                onClick = { onTagClick(tag.id) },
                label = { Text(text = tag.title, color = designSystem.composeColor(ColorToken.TEXT_PRIMARY)) },
                leadingIcon = { tag.icon?.let { Text(it) } },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = designSystem.composeColor(ColorToken.TEXT_PRIMARY).copy(alpha = 0.05f)
                ),
                border = BorderStroke(
                    width = designSystem.dimen(DimenToken.DIVIDER_THICKNESS).dp, 
                    color = designSystem.composeColor(ColorToken.TEXT_PRIMARY).copy(alpha = 0.1f)
                )
            )
        }
    }
}

@Composable
private fun RecordingActionButton(
    icon: IconToken,
    designSystem: DesignSystem,
    onClick: () -> Unit
) {
    FilledIconButton(
        onClick = onClick,
        modifier = Modifier.size(56.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = designSystem.composeColor(ColorToken.ACCENT_PRIMARY).copy(alpha = 0.8f)
        )
    ) {
        AppIcon(
            icon = designSystem.icon(icon),
            contentDescription = null,
            tint = designSystem.composeColor(ColorToken.TEXT_INVERTED)
        )
    }
}
