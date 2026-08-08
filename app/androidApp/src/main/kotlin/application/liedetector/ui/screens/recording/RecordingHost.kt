package application.liedetector.ui.screens.recording

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
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
import application.liedetector.ui.components.widgets.VoiceRecorderRenderer
import application.liedetector.uicore.widgets.UiWidget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingHost(component: RecordingComponent) {
    val viewModel = component.viewModel
    val state by viewModel.state.collectAsState()
    val designSystem = LocalDesignSystem.current
    val context = LocalContext.current
    
    // Stable derived states to prevent unnecessary recompositions

    val widgets = remember(state.widgets) { state.widgets }
    val activeRecorder = state.activeRecorder

    var showMenu by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            component.onMicClicked()
        }
    }

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
            // 1. Evidence List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding() + 16.dp,
                    bottom = padding.calculateBottomPadding() + 160.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    count = widgets.size,
                    key = { index -> widgets[index].id }
                ) { index ->
                    WidgetRenderer(
                        widget = widgets[index],
                        onAction = { },
                        component = component
                    )
                }
            }

            // 2. Control Panel Island
            BottomControlPanel(
                activeRecorder = activeRecorder,
                onMicClick = {
                    val permission = Manifest.permission.RECORD_AUDIO
                    if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                        component.onMicClicked()
                    } else {
                        permissionLauncher.launch(permission)
                    }
                },
                onToggle = { component.toggleRecording() },
                onStop = { component.stopRecording() }
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
                Icon(
                    imageVector = designSystem.icon(IconToken.ARROW_BACK),
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

@Composable
private fun BoxScope.BottomControlPanel(
    activeRecorder: UiWidget.VoiceRecorder?,
    onMicClick: () -> Unit,
    onToggle: () -> Unit,
    onStop: () -> Unit
) {
    val designSystem = LocalDesignSystem.current
    val hasActiveRecorder = activeRecorder != null

    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = hasActiveRecorder,
            transitionSpec = {
                (fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.8f, animationSpec = tween(400)))
                    .togetherWith(fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.8f, animationSpec = tween(300)))
            },
            label = "BottomBarTransition"
        ) { isRecording ->
            if (isRecording) {
                activeRecorder?.let { recorder ->
                    VoiceRecorderRenderer(
                        widget = recorder,
                        modifier = Modifier.fillMaxWidth(),
                        onToggle = onToggle,
                        onStop = onStop
                    )
                }
            } else {
                Surface(
                    color = designSystem.composeColor(ColorToken.GLASS_BASE).copy(alpha = 0.4f),
                    modifier = Modifier.widthIn(max = 340.dp),
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
                        RecordingActionButton(IconToken.MIC, designSystem, onMicClick)
                        RecordingActionButton(IconToken.GALLERY, designSystem) { }
                        RecordingActionButton(IconToken.NOTE, designSystem) { }
                    }
                }
            }
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
        Icon(
            imageVector = designSystem.icon(icon),
            contentDescription = null,
            tint = designSystem.composeColor(ColorToken.TEXT_INVERTED)
        )
    }
}
