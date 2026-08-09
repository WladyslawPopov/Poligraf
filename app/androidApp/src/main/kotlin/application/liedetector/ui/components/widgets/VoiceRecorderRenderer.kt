package application.liedetector.ui.components.widgets

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import application.liedetector.uicore.theme.LocalDesignSystem
import application.liedetector.uicore.theme.DesignSystem
import application.liedetector.uicore.theme.tokens.*
import application.liedetector.uicore.widgets.UiWidget
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.absoluteValue

private val AppleRed = Color(0xFFFF3B30)
private val AppleBlue = Color(0xFF007AFF)
private val AppleYellow = Color(0xFFFFD600)
private val DarkGrayBg = Color(0xFF1C1C1E)

@Composable
fun VoiceRecorderRenderer(
    widget: UiWidget.VoiceRecorder,
    modifier: Modifier = Modifier,
    onToggle: () -> Unit,
    onStop: () -> Unit,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onTrimUpdate: (Long, Long) -> Unit,
    onSave: () -> Unit,
    onResume: () -> Unit,
    onToggleTrim: () -> Unit,
    onSkip: (Long) -> Unit,
    onToggleExpand: () -> Unit,
    onTrimCancel: () -> Unit,
    onTrimApply: (Long, Long) -> Unit,
    onUploadFromFile: () -> Unit
) {
    val designSystem = LocalDesignSystem.current

    // Android specific: navigationBarsPadding and RoundedCornerShape(32.dp) for top corners
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        color = DarkGrayBg,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag Handle
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(48.dp, 16.dp)
                    .clickable { onToggleExpand() }
                    .padding(vertical = 5.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp, 5.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.3f))
                )
            }

            if (widget.isExpanded) {
                ExpandedContent(
                    widget, onToggle, onStop, onPlay, onPause,
                    onSeek, onSave, onToggleTrim, onSkip, designSystem,
                    onTrimCancel, onTrimApply, onResume, onUploadFromFile,
                    onTrimUpdate
                )
            } else {
                CollapsedContent(widget, onToggle, onResume, onPlay, onPause, onToggleExpand, designSystem)
            }
        }
    }
}

@Composable
private fun CollapsedContent(
    widget: UiWidget.VoiceRecorder,
    onToggle: () -> Unit,
    onResume: () -> Unit,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onToggleExpand: () -> Unit,
    designSystem: DesignSystem
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkGrayBg)
            .clickable { onToggleExpand() }
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Column (Title, Timer)
        Column(modifier = Modifier.weight(1.2f)) {
            Text(
                widget.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                maxLines = 1
            )
            Text(
                formatDurationSimple(widget.durationMillis),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp
            )
        }

        // Center: Waveform baseline + bars
        Box(modifier = Modifier
            .weight(2f)
            .height(32.dp)
            .padding(horizontal = 12.dp)) {
            Canvas(Modifier.fillMaxSize()) {
                val step = 4.dp.toPx()
                val centerY = size.height / 2f
                val count = (size.width / step).toInt()
                
                // Red base line
                drawLine(
                    color = AppleRed.copy(alpha = 0.3f),
                    start = Offset(0f, centerY),
                    end = Offset(size.width, centerY),
                    strokeWidth = 1.dp.toPx()
                )

                for (i in 0 until count) {
                    val x = i * step
                    // Calculate index relative to the end of amplitudes
                    val ampIndex = widget.amplitudes.size - count + i
                    val amp = if (widget.status == UiWidget.VoiceRecorder.Status.RECORDING && ampIndex >= 0) {
                        widget.amplitudes.getOrNull(ampIndex) ?: 0.05f
                    } else if (ampIndex >= 0 && ampIndex < widget.amplitudes.size) {
                        // Keep showing last amplitudes when paused
                        widget.amplitudes.getOrNull(ampIndex) ?: 0.05f
                    } else {
                        0.05f
                    }
                    val h = (amp * size.height).coerceAtLeast(2.dp.toPx())
                    drawRoundRect(
                        color = AppleRed,
                        topLeft = Offset(x, centerY - h / 2),
                        size = Size(2.dp.toPx(), h),
                        cornerRadius = CornerRadius(1.dp.toPx())
                    )
                }
            }
        }

        // Right: Action button
        val isRecording = widget.status == UiWidget.VoiceRecorder.Status.RECORDING
        val isPaused = widget.status == UiWidget.VoiceRecorder.Status.PAUSED
        val isReview = widget.status == UiWidget.VoiceRecorder.Status.REVIEW || widget.status == UiWidget.VoiceRecorder.Status.FINISHED
        
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (isRecording || isPaused) AppleRed else AppleBlue)
                .clickable { 
                    when {
                        isRecording -> onToggle() // Pause
                        isPaused -> onResume() // Record (append)
                        isReview -> {
                            if (widget.isPlaying) onPause() else onPlay()
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            val icon = when {
                isRecording -> designSystem.icon(IconToken.PAUSE)
                isPaused -> designSystem.icon(IconToken.MIC)
                else -> if (widget.isPlaying) designSystem.icon(IconToken.PAUSE) else designSystem.icon(IconToken.PLAY)
            }
            
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun ExpandedContent(
    widget: UiWidget.VoiceRecorder,
    onToggle: () -> Unit,
    onStop: () -> Unit,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSave: () -> Unit,
    onToggleTrim: () -> Unit,
    onSkip: (Long) -> Unit,
    designSystem: DesignSystem,
    onTrimCancel: () -> Unit,
    onTrimApply: (Long, Long) -> Unit,
    onResume: () -> Unit,
    onUploadFromFile: () -> Unit,
    onTrimUpdate: (Long, Long) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Toolbar
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)) {
            if (widget.isTrimming) {
                Text(
                    "Cancel",
                    color = Color.White,
                    fontSize = 17.sp,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clickable { onTrimCancel() }
                )
                Text(
                    "Trim",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
                Text(
                    "Apply",
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickable { onTrimApply(widget.trimStartMillis, widget.trimEndMillis) }
                )
            } else {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AppleBlue.copy(alpha = 0.15f))
                        .clickable { menuExpanded = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.MoreHoriz, contentDescription = null, tint = AppleBlue, modifier = Modifier.size(24.dp))
                    
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(DarkGrayBg).border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Загрузить аудио с файла", color = Color.White) },
                            onClick = { 
                                menuExpanded = false
                                onUploadFromFile()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Обрезать запись", color = Color.White) },
                            onClick = { 
                                menuExpanded = false
                                onToggleTrim()
                            }
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Text(
                        text = "${widget.title} ${formatTimeShort()}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = formatDurationSimple(widget.durationMillis),
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AppleBlue.copy(alpha = 0.15f))
                        .clickable { onSave() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        designSystem.icon(IconToken.CHECK), 
                        contentDescription = null, 
                        tint = AppleBlue, 
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Waveform area: Dark Gray (#1C1C1E) box with rounded corners (24.dp)
        // Waveform area: Integrated Ruler
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            ProfessionalWaveform(
                widget = widget,
                onSeek = onSeek
            )
        }

        if (widget.isTrimming) {
            Spacer(modifier = Modifier.height(32.dp))
            MiniTrimOverview(widget, onTrimUpdate)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Timer: Monospaced font
        val timerValue = if (widget.status == UiWidget.VoiceRecorder.Status.REVIEW || widget.isTrimming || widget.status == UiWidget.VoiceRecorder.Status.PAUSED) 
            widget.playbackPositionMillis 
        else 
            widget.durationMillis

        Text(
            text = formatDurationPrecise(timerValue),
            style = LocalTextStyle.current.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Normal,
                fontSize = 42.sp,
                letterSpacing = (-0.5).sp
            ),
            color = Color.White
        )

        Spacer(modifier = Modifier.height(32.dp))
        // Playback Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onSkip(-15000) }) {
                Icon(designSystem.icon(IconToken.HISTORY), contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
            }
            Spacer(modifier = Modifier.width(48.dp))
            IconButton(onClick = if (widget.isPlaying) onPause else onPlay) {
                Icon(
                    imageVector = if (widget.isPlaying) designSystem.icon(IconToken.PAUSE) else designSystem.icon(IconToken.PLAY),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(56.dp)
                )
            }
            Spacer(modifier = Modifier.width(48.dp))
            IconButton(onClick = { onSkip(15000) }) {
                Icon(designSystem.icon(IconToken.HISTORY), contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp).graphicsLayer(scaleX = -1f))
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (widget.isTrimming) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(horizontal = 32.dp, vertical = 12.dp)
                        .clickable { onToggleTrim() }
                ) {
                    Text("Trim", color = Color.White.copy(alpha = 0.6f), fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                }
                Text(
                    "Delete",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { /* Trigger Delete */ }
                )
            } else {
                Spacer(modifier = Modifier.width(48.dp))

                // Central Button: Mic (Record/Resume/Append) or Pause
                val isRecording = widget.status == UiWidget.VoiceRecorder.Status.RECORDING
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(AppleRed)
                        .clickable { 
                            if (isRecording) onToggle() 
                            else onResume() 
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRecording) designSystem.icon(IconToken.PAUSE) else designSystem.icon(IconToken.MIC),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(48.dp))
            }
        }
    }
}

@Composable
private fun MiniTrimOverview(
    widget: UiWidget.VoiceRecorder,
    onTrimUpdate: (Long, Long) -> Unit
) {
    // Local state for smooth dragging
    var localStart by remember(widget.isTrimming) { mutableLongStateOf(widget.trimStartMillis) }
    var localEnd by remember(widget.isTrimming) { mutableLongStateOf(widget.trimEndMillis) }

    val sidePadding = 32.dp // More space to let handles sit "outside" the track

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .height(56.dp)
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val sidePaddingPx = with(LocalDensity.current) { sidePadding.toPx() }
        val trackWidthPx = widthPx - 2 * sidePaddingPx

        // 1. Background Track
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = sidePadding)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Black.copy(alpha = 0.3f))
                .pointerInput(widget.durationMillis) {
                    detectTapGestures { offset ->
                        val tappedTrackX = (offset.x - sidePaddingPx).coerceIn(0f, trackWidthPx)
                        val tappedTime = (tappedTrackX / trackWidthPx * widget.durationMillis).toLong()
                        
                        val distStart = (tappedTime - localStart).absoluteValue
                        val distEnd = (tappedTime - localEnd).absoluteValue
                        
                        if (distStart < distEnd) {
                            localStart = tappedTime.coerceIn(0, localEnd - 100)
                        } else {
                            localEnd = tappedTime.coerceIn(localStart + 100, widget.durationMillis)
                        }
                        onTrimUpdate(localStart, localEnd)
                    }
                }
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val step = 2.dp.toPx()
                val centerY = size.height / 2f
                val count = (size.width / step).toInt()
                
                for (i in 0 until count) {
                    val x = i * step
                    val amp = widget.amplitudes.getOrNull((i.toFloat() / count * widget.amplitudes.size).toInt()) ?: 0.1f
                    val h = (amp * size.height * 0.4f).coerceAtLeast(1.dp.toPx())
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.2f),
                        topLeft = Offset(x, centerY - h / 2),
                        size = Size(1.dp.toPx(), h),
                        cornerRadius = CornerRadius(0.5.dp.toPx())
                    )
                }
            }
        }

        val startRatio = localStart.toFloat() / widget.durationMillis.coerceAtLeast(1)
        val endRatio = localEnd.toFloat() / widget.durationMillis.coerceAtLeast(1)
        val playbackRatio = widget.playbackPositionMillis.toFloat() / widget.durationMillis.coerceAtLeast(1)
        
        val leftX = sidePaddingPx + (trackWidthPx * startRatio)
        val rightX = sidePaddingPx + (trackWidthPx * endRatio)
        val playheadX = sidePaddingPx + (trackWidthPx * playbackRatio)

        // 2. Yellow Selection Frame
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(with(LocalDensity.current) { (rightX - leftX).toDp() })
                .offset(x = with(LocalDensity.current) { leftX.toDp() })
                .border(2.dp, AppleYellow)
        )

        // 3. Left Handle (Sitting OUTSIDE to the left of leftX)
        // Handle Box is 48dp, yellow bar is 20dp. 
        // We want the yellow bar's RIGHT edge to be at leftX.
        // Yellow bar center is at 24dp. Right edge is at 24 + 10 = 34dp.
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(48.dp)
                .offset(x = with(LocalDensity.current) { (leftX - 34.dp.toPx()).toDp() })
                .pointerInput(widget.durationMillis) {
                    var accumulatedDrag = 0f
                    detectDragGestures(
                        onDragStart = { accumulatedDrag = 0f },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            accumulatedDrag += dragAmount.x
                            val deltaMillis = (accumulatedDrag / trackWidthPx * widget.durationMillis).toLong()
                            val newStart = (localStart + deltaMillis).coerceIn(0, localEnd - 100)
                            if (newStart != localStart) {
                                localStart = newStart
                                onTrimUpdate(localStart, localEnd)
                                accumulatedDrag = 0f
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.fillMaxHeight().width(20.dp).background(AppleYellow, RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
            }
        }

        // 4. Right Handle (Sitting OUTSIDE to the right of rightX)
        // Yellow bar's LEFT edge should be at rightX.
        // Yellow bar center is at 24dp. Left edge is at 24 - 10 = 14dp.
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(48.dp)
                .offset(x = with(LocalDensity.current) { (rightX - 14.dp.toPx()).toDp() })
                .pointerInput(widget.durationMillis) {
                    var accumulatedDrag = 0f
                    detectDragGestures(
                        onDragStart = { accumulatedDrag = 0f },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            accumulatedDrag += dragAmount.x
                            val deltaMillis = (accumulatedDrag / trackWidthPx * widget.durationMillis).toLong()
                            val newEnd = (localEnd + deltaMillis).coerceIn(localStart + 100, widget.durationMillis)
                            if (newEnd != localEnd) {
                                localEnd = newEnd
                                onTrimUpdate(localStart, localEnd)
                                accumulatedDrag = 0f
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.fillMaxHeight().width(20.dp).background(AppleYellow, RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
            }
        }

        // 5. Playhead indicator
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(2.dp)
                .offset(x = with(LocalDensity.current) { (playheadX - 1.dp.toPx()).toDp() })
                .background(AppleBlue)
        )
    }
}

@Composable
private fun ProfessionalWaveform(
    widget: UiWidget.VoiceRecorder,
    onSeek: (Long) -> Unit
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val barWidthPx = with(density) { 2.dp.toPx() }
    val stepPx = with(density) { 6.dp.toPx() }
    
    // Crucial: This must match the speed at which amplitudes are collected.
    // If it's too fast, the waveform runs ahead. If too slow, it lags.
    // Standard is ~33ms, but we'll use 100ms to reduce density as requested.
    val millisPerBar = 100f 

    val scrollOffset = remember { Animatable(0f) }
    var isDragging by remember { mutableStateOf(false) }

    // Position in index units
    val currentIdx = if (widget.status == UiWidget.VoiceRecorder.Status.RECORDING) {
        widget.durationMillis.toFloat() / millisPerBar
    } else {
        widget.playbackPositionMillis.toFloat() / millisPerBar
    }

    LaunchedEffect(currentIdx, isDragging) {
        if (!isDragging) {
            scrollOffset.snapTo(currentIdx)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .draggable(
                state = rememberDraggableState { delta ->
                    if (widget.status == UiWidget.VoiceRecorder.Status.REVIEW || widget.status == UiWidget.VoiceRecorder.Status.PAUSED) {
                        isDragging = true
                        val maxIdx = widget.durationMillis.toFloat() / millisPerBar
                        scope.launch {
                            val dragAmount = delta / stepPx
                            val currentVal = scrollOffset.value
                            val resistance = if (currentVal < 0 || currentVal > maxIdx) 0.3f else 1f
                            val nextVal = currentVal - (dragAmount * resistance)
                            
                            scrollOffset.snapTo(nextVal)
                            onSeek((nextVal.coerceIn(0f, maxIdx) * millisPerBar).toLong())
                        }
                    }
                },
                orientation = Orientation.Horizontal,
                onDragStopped = { velocity ->
                    if (widget.status == UiWidget.VoiceRecorder.Status.REVIEW || widget.status == UiWidget.VoiceRecorder.Status.PAUSED) {
                        val maxIdx = widget.durationMillis.toFloat() / millisPerBar
                        scope.launch {
                            isDragging = false
                            if (scrollOffset.value < 0) {
                                scrollOffset.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
                            } else if (scrollOffset.value > maxIdx) {
                                scrollOffset.animateTo(maxIdx, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
                            } else {
                                val decay = exponentialDecay<Float>(frictionMultiplier = 1.5f)
                                scrollOffset.animateDecay(
                                    initialVelocity = -velocity / stepPx,
                                    animationSpec = decay
                                )
                                // After decay, if we landed outside, bounce back
                                if (scrollOffset.value < 0) {
                                    scrollOffset.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                } else if (scrollOffset.value > maxIdx) {
                                    scrollOffset.animateTo(maxIdx, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                }
                            }
                            onSeek((scrollOffset.value.coerceIn(0f, maxIdx) * millisPerBar).toLong())
                        }
                    }
                }
            )
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val headX = widthPx / 2f

        Canvas(modifier = Modifier.fillMaxSize()) {
            val amplitudes = widget.amplitudes
            val activeIdx = scrollOffset.value
            val timelineHeight = 48.dp.toPx()
            val waveformHeight = heightPx - timelineHeight
            val centerY = waveformHeight / 2f
            
            // startX is where the recording starts (time 0)
            val startX = headX - (activeIdx * stepPx)

            // LAYER 1: Backgrounds
            // Future area background
            drawRect(
                color = Color.Black,
                topLeft = Offset(0f, 0f),
                size = Size(widthPx, waveformHeight)
            )

            // Recorded part background (attached to the strip)
            val recordedWidth = (widget.durationMillis.toFloat() / millisPerBar) * stepPx
            
            drawRect(
                color = Color(0xFF2C2C2E),
                topLeft = Offset(startX, 0f),
                size = Size(recordedWidth, waveformHeight)
            )

            // LAYER 2: Yellow Highlight for Trim
            if (widget.isTrimming) {
                val trimStartIdx = widget.trimStartMillis.toFloat() / millisPerBar
                val trimEndIdx = widget.trimEndMillis.toFloat() / millisPerBar
                
                val trimStartX = startX + trimStartIdx * stepPx
                val trimEndX = startX + trimEndIdx * stepPx
                
                drawRect(
                    color = AppleYellow.copy(alpha = 0.25f),
                    topLeft = Offset(trimStartX, 0f),
                    size = Size((trimEndX - trimStartX).coerceAtLeast(1f), waveformHeight)
                )
            }

            // LAYER 3: Waveform Bars
            // Calculate indices of bars that fall within the visible widthPx
            val firstVisibleBarIdx = ((-startX) / stepPx).toInt().coerceAtLeast(0)
            val lastVisibleBarIdx = ((widthPx - startX) / stepPx).toInt().coerceAtMost((widget.durationMillis / millisPerBar).toInt())

            for (i in firstVisibleBarIdx..lastVisibleBarIdx) {
                val x = startX + (i.toFloat() * stepPx)
                
                // Map drawing slot i to the nearest sample in amplitudes (sampled at 33ms)
                val ampIdx = (i * millisPerBar / 33.33f).toInt()
                val amp = amplitudes.getOrElse(ampIdx) { 0.05f }
                
                val barHeight = (amp * (waveformHeight * 0.55f)).coerceAtLeast(2.dp.toPx())
                
                drawLine(
                    color = Color.White,
                    start = Offset(x, centerY - barHeight / 2),
                    end = Offset(x, centerY + barHeight / 2),
                    strokeWidth = barWidthPx,
                    cap = StrokeCap.Round
                )
            }

            // LAYER 4: Ruler (Directly on the panel background)
            val rulerY = waveformHeight
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                alpha = 90
                textSize = 10.dp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
                typeface = android.graphics.Typeface.create("sans-serif-light", android.graphics.Typeface.NORMAL)
            }

            // Separator
            drawLine(
                color = Color.White.copy(alpha = 0.15f),
                start = Offset(0f, rulerY),
                end = Offset(widthPx, rulerY),
                strokeWidth = 1.dp.toPx()
            )

            // Tick logic: Draw every 200ms
            val ticksPerSecond = 5
            val tickIntervalMillis = 1000f / ticksPerSecond
            
            // Range of ticks to draw based on visible time
            val firstTick = ((activeIdx * millisPerBar - headX / stepPx * millisPerBar) / tickIntervalMillis).toInt().coerceAtLeast(0)
            val lastTick = ((activeIdx * millisPerBar + headX / stepPx * millisPerBar) / tickIntervalMillis).toInt()

            for (t in firstTick..lastTick) {
                val timeMillis = t * tickIntervalMillis
                val x = startX + (timeMillis / millisPerBar) * stepPx
                
                val isSecond = t % ticksPerSecond == 0
                val tickHeight = if (isSecond) 6.dp.toPx() else 3.dp.toPx()
                
                drawLine(
                    color = Color.White.copy(alpha = if (isSecond) 0.3f else 0.1f),
                    start = Offset(x, rulerY),
                    end = Offset(x, rulerY + tickHeight),
                    strokeWidth = 1.dp.toPx()
                )

                if (isSecond) {
                    val sec = t / ticksPerSecond
                    val timeLabel = "%d:%02d".format(sec / 60, sec % 60)
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawText(timeLabel, x, rulerY + 24.dp.toPx(), textPaint)
                    }
                }
            }

            // LAYER 5: Playhead
            val playheadColor = if (widget.status == UiWidget.VoiceRecorder.Status.RECORDING) AppleRed else AppleBlue
            
            drawLine(
                color = playheadColor,
                start = Offset(headX, 0f),
                end = Offset(headX, waveformHeight),
                strokeWidth = 2.dp.toPx()
            )
            drawCircle(playheadColor, radius = 3.5.dp.toPx(), center = Offset(headX, 0f))
            drawCircle(playheadColor, radius = 3.5.dp.toPx(), center = Offset(headX, waveformHeight))
        }
    }
}

private fun formatDurationPrecise(millis: Long): String {
    val ms = (millis % 1000) / 10
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    return "%02d:%02d,%02d".format(minutes, seconds, ms)
}

private fun formatDurationSimple(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60))
    return "%d:%02d".format(minutes, seconds)
}

private fun formatTimeShort(): String {
    val now = Calendar.getInstance()
    val hour = now.get(Calendar.HOUR_OF_DAY)
    val minute = now.get(Calendar.MINUTE)
    return "%02d:%02d".format(hour, minute)
}
