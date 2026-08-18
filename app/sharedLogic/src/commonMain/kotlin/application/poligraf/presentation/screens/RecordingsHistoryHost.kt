package application.poligraf.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.*
import application.poligraf.widgets.AppScaffold
import application.poligraf.widgets.utils.composeColor
import application.poligraf.widgets.utils.AppIcon
import application.poligraf.widgets.recorder.VoiceRecorderRenderer
import application.poligraf.presentation.recordingHistory.RecordingsHistoryComponent
import application.poligraf.uicore.state.VoiceRecorderAction
import application.poligraf.uicore.theme.LocalDesignSystem
import application.poligraf.uicore.theme.tokens.ColorToken
import application.poligraf.uicore.theme.tokens.DimenToken
import application.poligraf.uicore.theme.tokens.IconToken
import application.poligraf.uicore.theme.tokens.StringToken
import application.poligraf.uicore.widgets.VoiceRecorder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingsHistoryHost(component: RecordingsHistoryComponent) {
    val viewModel = component.viewModel
    val activeRecorder by viewModel.activeRecorder.collectAsState()
    val recorderUiState by viewModel.recorderUiState.collectAsState()
    val historicalRecordings by viewModel.historicalRecordings.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val state by viewModel.state.collectAsState()
    
    val designSystem = LocalDesignSystem.current
    
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = if (activeRecorder != null) SheetValue.Expanded else SheetValue.Hidden,
            skipHiddenState = false,
            confirmValueChange = { targetValue ->
                if (targetValue == SheetValue.Hidden) {
                    // Only discard if we are NOT recording
                    if (!recorderUiState.waveform.isRecording) {
                        viewModel.handleAction(VoiceRecorderAction.DiscardActive)
                        true
                    } else {
                        false
                    }
                } else {
                    true
                }
            }
        )
    )

    // Sync sheet state with activeRecorder
    LaunchedEffect(activeRecorder?.id) {
        if (activeRecorder != null) {
            // Force expand whenever a new recorder is activated
            scaffoldState.bottomSheetState.expand()
        } else {
            if (scaffoldState.bottomSheetState.currentValue != SheetValue.Hidden) {
                scaffoldState.bottomSheetState.hide()
            }
        }
    }

    AppScaffold(
        viewModel = viewModel,
        state = state,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = designSystem.string(StringToken.RECORDER_HISTORY_TITLE),
                        color = designSystem.composeColor(ColorToken.TEXT_PRIMARY),
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { component.goBack() }) {
                        AppIcon(
                            icon = designSystem.icon(IconToken.ARROW_BACK),
                            contentDescription = null, 
                            tint = designSystem.composeColor(ColorToken.TEXT_PRIMARY)
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(designSystem.composeColor(ColorToken.GLASS_BASE).copy(alpha = 0.25f))
                            .clickable { viewModel.handleAction(VoiceRecorderAction.ToggleSelectionMode) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = designSystem.string(
                                if (isSelectionMode) StringToken.RECORDER_CANCEL else StringToken.RECORDER_HISTORY_SELECT
                            ),
                            color = designSystem.composeColor(ColorToken.ACCENT_PRIMARY),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        BottomSheetScaffold(
            modifier = Modifier.fillMaxSize(),
            scaffoldState = scaffoldState,
            sheetPeekHeight = 0.dp,
            sheetContainerColor = Color.Transparent,
            sheetContentColor = designSystem.composeColor(ColorToken.TEXT_PRIMARY),
            sheetDragHandle = null,
            sheetSwipeEnabled = activeRecorder != null && !recorderUiState.waveform.isRecording,
            sheetContent = {
                // Limit sheet height so it doesn't cover toolbar when expanded
                Box(modifier = Modifier.padding(top = innerPadding.calculateTopPadding())) {
                    key(activeRecorder?.id) {
                        activeRecorder?.let { 
                            VoiceRecorderRenderer(
                                state = recorderUiState,
                                onAction = { action ->
                                    viewModel.handleAction(action)
                                }
                            )
                        } ?: Box(Modifier.fillMaxWidth().height(1.dp))
                    }
                }
            },
            containerColor = Color.Transparent
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                if (historicalRecordings.isEmpty() && activeRecorder == null) {
                    EmptyHistoryView()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = innerPadding.calculateTopPadding() + 16.dp, 
                            bottom = 200.dp
                        )
                    ) {
                        items(historicalRecordings) { recorder ->
                            RecordingListItem(
                                recorder = recorder,
                                isSelected = selectedIds.contains(recorder.id),
                                isSelectionMode = isSelectionMode,
                                onClick = { 
                                    if (isSelectionMode) {
                                        viewModel.handleAction(VoiceRecorderAction.ToggleItemSelection(recorder.id))
                                    } else {
                                        component.onRecordingClicked(recorder)
                                    }
                                }
                            )
                        }
                    }
                }

                // Selection Actions Bar
                if (isSelectionMode && selectedIds.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 32.dp)
                            .padding(horizontal = 24.dp)
                            .fillMaxWidth(),
                        color = designSystem.composeColor(ColorToken.RECORDER_SURFACE).copy(alpha = 0.9f),
                        shape = RoundedCornerShape(24.dp),
                        tonalElevation = 8.dp,
                        border = BorderStroke(1.dp, designSystem.composeColor(ColorToken.TEXT_PRIMARY).copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Selected: ${selectedIds.size}",
                                color = designSystem.composeColor(ColorToken.TEXT_PRIMARY),
                                fontWeight = FontWeight.Bold
                            )
                            
                            IconButton(
                                onClick = { viewModel.handleAction(VoiceRecorderAction.DeleteSelected) },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(designSystem.composeColor(ColorToken.RECORDER_PRIMARY).copy(alpha = 0.1f))
                            ) {
                                AppIcon(
                                    icon = designSystem.icon(IconToken.DELETE),
                                    contentDescription = null,
                                    tint = designSystem.composeColor(ColorToken.RECORDER_PRIMARY)
                                )
                            }
                        }
                    }
                }

                // Professional BIG RED BUTTON for recording
                val isSheetHidden = scaffoldState.bottomSheetState.currentValue == SheetValue.Hidden
                if (!isSelectionMode && (activeRecorder == null || isSheetHidden)) {
                    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "Scale"
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = designSystem.dimen(DimenToken.SPACING_XL).dp * 2)
                            .graphicsLayer(scaleX = scale, scaleY = scale)
                            .size(designSystem.dimen(DimenToken.SUBJECT_CARD_ICON_SIZE).dp - 10.dp)
                            .border(
                                width = 4.dp, 
                                color = designSystem.composeColor(ColorToken.TEXT_PRIMARY).copy(alpha = 0.1f), 
                                shape = CircleShape
                            )
                            .padding(designSystem.dimen(DimenToken.SPACING_SMALL).dp - 2.dp)
                            .clip(CircleShape)
                            .background(designSystem.composeColor(ColorToken.RECORDER_WAVEFORM))
                            .clickable { 
                                component.onMicClicked()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AppIcon(
                            icon = designSystem.icon(IconToken.MIC),
                            contentDescription = null,
                            tint = designSystem.composeColor(ColorToken.TEXT_INVERTED),
                            modifier = Modifier.size(designSystem.dimen(DimenToken.SPACING_XL).dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyHistoryView() {
    val designSystem = LocalDesignSystem.current
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = designSystem.string(StringToken.RECORDER_HISTORY_EMPTY),
            style = MaterialTheme.typography.headlineLarge,
            color = designSystem.composeColor(ColorToken.TEXT_PRIMARY).copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun RecordingListItem(
    recorder: VoiceRecorder,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit
) {
    val designSystem = LocalDesignSystem.current
    val scale by animateFloatAsState(if (isSelected) 0.98f else 1f, label = "Scale")
    
    Box(
        modifier = Modifier
            .padding(horizontal = designSystem.dimen(DimenToken.SPACING_MEDIUM).dp, vertical = 4.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) 
                    designSystem.composeColor(ColorToken.RECORDER_PRIMARY).copy(alpha = 0.1f)
                else 
                    designSystem.composeColor(ColorToken.GLASS_BASE).copy(alpha = 0.15f)
            )
            .border(
                width = 2.dp,
                color = if (isSelected) 
                    designSystem.composeColor(ColorToken.RECORDER_PRIMARY).copy(alpha = 0.5f)
                else 
                    Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = designSystem.dimen(DimenToken.SPACING_LARGE).dp,
                    vertical = designSystem.dimen(DimenToken.SPACING_MEDIUM).dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) 
                                designSystem.composeColor(ColorToken.RECORDER_PRIMARY)
                            else 
                                designSystem.composeColor(ColorToken.TEXT_PRIMARY).copy(alpha = 0.1f)
                        )
                        .border(
                            1.dp, 
                            designSystem.composeColor(ColorToken.TEXT_PRIMARY).copy(alpha = 0.2f), 
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        AppIcon(
                            icon = designSystem.icon(IconToken.CHECK),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recorder.title,
                    color = designSystem.composeColor(ColorToken.TEXT_PRIMARY),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
                Text(
                    text = designSystem.string(StringToken.RECORDER_TODAY),
                    color = designSystem.composeColor(ColorToken.TEXT_PRIMARY).copy(alpha = 0.4f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }
            
            Text(
                text = formatDurationCompact(recorder.durationMillis),
                color = designSystem.composeColor(ColorToken.TEXT_PRIMARY).copy(alpha = 0.4f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatDurationCompact(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}
