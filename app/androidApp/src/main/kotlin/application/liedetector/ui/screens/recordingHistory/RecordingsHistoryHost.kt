package application.liedetector.ui.screens.recordingHistory

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import application.liedetector.presentation.recordingHistory.RecordingsHistoryComponent
import application.liedetector.ui.components.AppScaffold
import application.liedetector.uicore.theme.LocalDesignSystem
import application.liedetector.uicore.theme.tokens.ColorToken
import application.liedetector.uicore.theme.tokens.IconToken
import application.liedetector.theme.utils.composeColor
import application.liedetector.ui.components.widgets.VoiceRecorderRenderer
import application.liedetector.uicore.widgets.UiWidget
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingsHistoryHost(component: RecordingsHistoryComponent) {
    val viewModel = component.viewModel
    val state by viewModel.state.collectAsState()
    val designSystem = LocalDesignSystem.current
    val scope = rememberCoroutineScope()
    
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = if (state.activeRecorder != null) SheetValue.PartiallyExpanded else SheetValue.Hidden,
            skipHiddenState = false,
            confirmValueChange = { newValue ->
                val isRecording = state.activeRecorder?.status == UiWidget.VoiceRecorder.Status.RECORDING
                !(isRecording && newValue == SheetValue.Hidden) // Prevent hiding while recording
            }
        )
    )

    // Sync sheet state with activeRecorder
    LaunchedEffect(state.activeRecorder) {
        if (state.activeRecorder != null) {
            // If we just got a new recorder (e.g. started recording), expand it
            if (scaffoldState.bottomSheetState.currentValue == SheetValue.Hidden) {
                scaffoldState.bottomSheetState.partialExpand()
            }
        } else {
            // Only hide if we explicitly cleared the recorder
            if (scaffoldState.bottomSheetState.currentValue != SheetValue.Hidden) {
                scaffoldState.bottomSheetState.hide()
            }
        }
    }

    // Sync expanded state
    LaunchedEffect(scaffoldState.bottomSheetState.currentValue) {
        val isExpanded = scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded
        if (state.activeRecorder?.isExpanded != isExpanded) {
            viewModel.toggleExpand()
        }
    }

    AppScaffold(
        viewModel = viewModel,
        state = state,
        topBar = {
            TopAppBar(
                title = { Text("All Recordings", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { component.goBack() }) {
                        Icon(designSystem.icon(IconToken.ARROW_BACK), contentDescription = null, tint = Color.White)
                    }
                },
                actions = {
                    TextButton(onClick = { }) {
                        Text("Select", color = designSystem.composeColor(ColorToken.ACCENT_PRIMARY))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        BottomSheetScaffold(
            modifier = Modifier.padding(top = innerPadding.calculateTopPadding()),
            scaffoldState = scaffoldState,
            sheetPeekHeight = 120.dp,
            sheetContainerColor = Color.Transparent,
            sheetContentColor = Color.White,
            sheetDragHandle = null,
            sheetSwipeEnabled = state.activeRecorder != null,
            sheetContent = {
                state.activeRecorder?.let { recorder ->
                    VoiceRecorderRenderer(
                        widget = recorder,
                        onToggle = { component.toggleRecording() },
                        onStop = { component.stopRecording() },
                        onPlay = { component.onPlayClicked() },
                        onPause = { component.onPausePlaybackClicked() },
                        onSeek = { component.onSeek(it) },
                        onTrimUpdate = { start, end -> component.onTrimUpdate(start, end) },
                        onSave = { component.onSaveClicked() },
                        onResume = { component.onResumeRecording() },
                        onToggleTrim = { component.toggleTrimMode() },
                        onSkip = { component.onSkip(it) },
                        onToggleExpand = {
                            scope.launch {
                                if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                                    scaffoldState.bottomSheetState.partialExpand()
                                } else {
                                    scaffoldState.bottomSheetState.expand()
                                }
                            }
                        },
                        onTrimCancel = { component.onTrimCancel() },
                        onTrimApply = { start, end -> component.onTrim(start, end) },
                        onUploadFromFile = { component.onUploadFromFileClicked() }
                    )
                } ?: Box(Modifier.fillMaxWidth().height(1.dp))
            },
            containerColor = Color.Transparent
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                val recorderWidgets = state.widgets.filterIsInstance<UiWidget.VoiceRecorder>()

                if (recorderWidgets.isEmpty() && state.activeRecorder == null) {
                    EmptyHistoryView()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 200.dp)
                    ) {
                        items(recorderWidgets) { recorder ->
                            RecordingListItem(recorder) {
                                component.onRecordingClicked(recorder)
                            }
                        }
                    }
                }

                // Professional BIG RED BUTTON for recording
                val isSheetHidden = scaffoldState.bottomSheetState.currentValue == SheetValue.Hidden
                if (state.activeRecorder == null || isSheetHidden) {
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
                            .padding(bottom = 64.dp)
                            .graphicsLayer(scaleX = scale, scaleY = scale)
                            .size(80.dp)
                            .border(4.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                            .padding(6.dp)
                            .clip(CircleShape)
                            .background(designSystem.composeColor(ColorToken.RECORDER_WAVEFORM))
                            .clickable { 
                                component.onMicClicked()
                                if (isSheetHidden) {
                                    scope.launch { scaffoldState.bottomSheetState.partialExpand() }
                                }
                            },
                        contentAlignment = Alignment.Center
                    )
{
                        Icon(
                            designSystem.icon(IconToken.MIC),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyHistoryView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Make your first\nrecord 🎙️",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun RecordingListItem(
    recorder: UiWidget.VoiceRecorder,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 14.dp)
    ) {
        Text(recorder.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Today", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
            Text(formatDurationCompact(recorder.durationMillis), color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 0.5.dp)
    }
}

private fun formatDurationCompact(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    return "%02d:%02d".format(minutes, seconds)
}
