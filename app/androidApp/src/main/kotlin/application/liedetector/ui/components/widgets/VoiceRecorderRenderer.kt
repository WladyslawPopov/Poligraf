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
    onTrimApply: (Long, Long) -> Unit
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
                    onTrimCancel, onTrimApply, onResume
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
    onResume: () -> Unit
) {
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
                    color = AppleBlue,
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
                    color = AppleBlue,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickable { onTrimApply(widget.trimStartMillis, widget.trimEndMillis) }
                )
            } else {
                IconButton(
                    onClick = { /* More options */ },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(Icons.Default.MoreHoriz, contentDescription = null, tint = AppleBlue)
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Text(widget.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    // Subtitle: "HH:MM duration"
                    Text("${formatTimeShort()}   ${formatDurationSimple(widget.durationMillis)}",
                        color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
                }
                IconButton(
                    onClick = onSave,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(designSystem.icon(IconToken.CHECK), contentDescription = null, tint = AppleBlue, modifier = Modifier.size(28.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Waveform area: Dark Gray (#1C1C1E) box with rounded corners (24.dp)
        if (!widget.isTrimming) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(DarkGrayBg)
            ) {
                ProfessionalWaveform(
                    widget = widget,
                    waveformColor = Color.White.copy(alpha = 0.3f),
                    onSeek = onSeek
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Timer: Monospaced font
        val timerValue = if (widget.status == UiWidget.VoiceRecorder.Status.REVIEW || widget.isTrimming) 
            widget.playbackPositionMillis 
        else 
            widget.durationMillis

        Text(
            text = formatDurationPrecise(timerValue),
            style = LocalTextStyle.current.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                fontSize = 72.sp
            ),
            color = Color.White
        )

        if (widget.isTrimming) {
            Spacer(modifier = Modifier.height(48.dp))
            MiniTrimOverview(widget)
            Spacer(modifier = Modifier.height(48.dp))
        } else if (widget.status == UiWidget.VoiceRecorder.Status.REVIEW || widget.status == UiWidget.VoiceRecorder.Status.PAUSED) {
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
                        .clickable { /* Trigger Trim Action */ }
                ) {
                    Text("Trim", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                }
                Text(
                    "Delete",
                    color = AppleRed,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { /* Trigger Delete */ }
                )
            } else {
                IconButton(onClick = { /* Notes */ }) {
                    Icon(designSystem.icon(IconToken.NOTE), contentDescription = null, tint = AppleBlue, modifier = Modifier.size(28.dp))
                }

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

                IconButton(onClick = onToggleTrim) {
                    Icon(designSystem.icon(IconToken.SETTINGS), contentDescription = null, tint = AppleBlue, modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}

@Composable
private fun MiniTrimOverview(
    widget: UiWidget.VoiceRecorder
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(80.dp)
    ) {
        val width = maxWidth
        
        Canvas(Modifier.fillMaxSize()) {
            val step = 3.dp.toPx()
            val centerY = size.height / 2f
            val count = (size.width / step).toInt()
            
            for (i in 0 until count) {
                val x = i * step
                val amp = widget.amplitudes.getOrNull((i.toFloat() / count * widget.amplitudes.size).toInt()) ?: 0.1f
                val h = (amp * size.height * 0.6f).coerceAtLeast(2.dp.toPx())
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.2f),
                    topLeft = Offset(x, centerY - h / 2),
                    size = Size(2.dp.toPx(), h),
                    cornerRadius = CornerRadius(1.dp.toPx())
                )
            }
        }

        val startRatio = widget.trimStartMillis.toFloat() / widget.durationMillis.coerceAtLeast(1)
        val endRatio = widget.trimEndMillis.toFloat() / widget.durationMillis.coerceAtLeast(1)
        
        val left = width * startRatio
        val right = width * endRatio

        // Dark overlays for excluded parts
        Box(modifier = Modifier.fillMaxHeight().width(left).background(Color.Black.copy(alpha = 0.6f)))
        Box(modifier = Modifier.fillMaxHeight().width(width - right).offset(x = right).background(Color.Black.copy(alpha = 0.6f)))

        // Yellow Trim Box (selection)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(right - left)
                .offset(x = left)
                .border(2.dp, AppleYellow)
        )

        // Yellow Handles (iOS style with thick bars)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(16.dp)
                .offset(x = left - 8.dp)
                .background(AppleYellow, RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
        )
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(16.dp)
                .offset(x = right - 8.dp)
                .background(AppleYellow, RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
        )
    }
}

@Composable
private fun ProfessionalWaveform(
    widget: UiWidget.VoiceRecorder,
    waveformColor: Color,
    onSeek: (Long) -> Unit
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val barWidthPx = with(density) { 3.dp.toPx() }
    val stepPx = with(density) { 6.dp.toPx() }

    val scrollOffset = remember { Animatable(0f) }
    var isDragging by remember { mutableStateOf(false) }

    val currentIdx = if (widget.status == UiWidget.VoiceRecorder.Status.RECORDING) {
        widget.durationMillis.toFloat() / 33f
    } else {
        widget.playbackPositionMillis.toFloat() / 33f
    }

    LaunchedEffect(currentIdx, isDragging) {
        if (!isDragging) {
            scrollOffset.snapTo(currentIdx)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGrayBg)
            .draggable(
                state = rememberDraggableState { delta ->
                    if (widget.status == UiWidget.VoiceRecorder.Status.REVIEW || widget.status == UiWidget.VoiceRecorder.Status.PAUSED) {
                        isDragging = true
                        scope.launch {
                            val newOffset = scrollOffset.value - (delta / stepPx)
                            scrollOffset.snapTo(newOffset.coerceAtLeast(0f))
                            onSeek((scrollOffset.value * 33).toLong())
                        }
                    }
                },
                orientation = Orientation.Horizontal,
                onDragStopped = { velocity ->
                    if (widget.status == UiWidget.VoiceRecorder.Status.REVIEW || widget.status == UiWidget.VoiceRecorder.Status.PAUSED) {
                        scope.launch {
                            val decay = exponentialDecay<Float>(frictionMultiplier = 1.2f)
                            scrollOffset.animateDecay(
                                initialVelocity = -velocity / stepPx,
                                animationSpec = decay
                            )
                            isDragging = false
                            onSeek((scrollOffset.value * 33).toLong())
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

            val firstVisibleIdx = (activeIdx - (headX / stepPx) - 10).toInt().coerceAtLeast(0)
            val lastVisibleIdx = (activeIdx + (headX / stepPx) + 10).toInt().coerceAtMost(amplitudes.size - 1)

            for (i in firstVisibleIdx..lastVisibleIdx) {
                val amp = amplitudes.getOrElse(i) { 0.1f }
                val x = headX + (i - activeIdx) * stepPx
                val barHeight = (amp * (heightPx * 0.7f)).coerceAtLeast(4.dp.toPx())
                
                drawRoundRect(
                    color = waveformColor,
                    topLeft = Offset(x, (heightPx - barHeight) / 2),
                    size = Size(barWidthPx, barHeight),
                    cornerRadius = CornerRadius(barWidthPx / 2)
                )
            }

            // Time Markers
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                alpha = 80
                textSize = 11.dp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
            }

            val millisPerBar = 33f
            val barsPerSecond = 1000f / millisPerBar
            val startSec = ((activeIdx - headX / stepPx) / barsPerSecond).toInt().coerceAtLeast(0)
            val endSec = ((activeIdx + headX / stepPx) / barsPerSecond).toInt()

            for (sec in startSec..endSec) {
                val x = headX + (sec * barsPerSecond - activeIdx) * stepPx
                val timeLabel = "%d:%02d".format(sec / 60, sec % 60)
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawText(timeLabel, x, heightPx - 10.dp.toPx(), paint)
                }
                // Small tick
                drawLine(
                    color = Color.White.copy(alpha = 0.2f),
                    start = Offset(x, heightPx - 28.dp.toPx()),
                    end = Offset(x, heightPx - 32.dp.toPx()),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Playhead color logic: Red for recording, Blue for review/trim
            val playheadColor = if (widget.status == UiWidget.VoiceRecorder.Status.RECORDING) AppleRed else AppleBlue

            drawLine(
                color = playheadColor,
                start = Offset(headX, 0f),
                end = Offset(headX, heightPx - 32.dp.toPx()), // Stop above time markers
                strokeWidth = 2.dp.toPx()
            )
            drawCircle(playheadColor, radius = 4.dp.toPx(), center = Offset(headX, 0f))
            drawCircle(playheadColor, radius = 4.dp.toPx(), center = Offset(headX, heightPx - 32.dp.toPx()))
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
