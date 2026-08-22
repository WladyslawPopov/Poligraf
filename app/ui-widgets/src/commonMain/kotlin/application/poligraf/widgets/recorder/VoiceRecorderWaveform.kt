package application.poligraf.widgets.recorder

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import application.poligraf.engine.io.audio.AudioConstants
import application.poligraf.uicore.state.VoiceRecorderAction
import application.poligraf.uicore.state.VoiceRecorderUiState
import application.poligraf.uicore.theme.DesignSystem
import application.poligraf.uicore.theme.tokens.ColorToken
import application.poligraf.uicore.theme.tokens.DimenToken
import application.poligraf.uicore.theme.tokens.TypographyToken
import application.poligraf.widgets.utils.typography
import kotlinx.coroutines.launch

@Composable
fun VoiceRecorderWaveform(
    state: VoiceRecorderUiState,
    onAction: (VoiceRecorderAction) -> Unit,
    designSystem: DesignSystem,
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val stepPx = with(density) { 6.dp.toPx() }
    val millisPerBar = AudioConstants.WAVEFORM_STEP_MS

    val scrollOffset = remember { Animatable(0f) }
    var isDragging by remember { mutableStateOf(false) }
    val textMeasurer = rememberTextMeasurer()

    val dataStyle = designSystem.typography(TypographyToken.DATA_NUMERIC)
    val currentIdx = if (state.waveform.isRecording) {
        state.waveform.durationMillis.toFloat() / millisPerBar
    } else {
        state.waveform.playbackPositionMillis.toFloat() / millisPerBar
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
                    if (!state.waveform.isRecording) {
                        isDragging = true
                        val maxIdx = state.waveform.durationMillis.toFloat() / millisPerBar
                        scope.launch {
                            val dragAmount = delta / stepPx
                            val currentVal = scrollOffset.value
                            val resistance = if ((currentVal < 0f) || (currentVal > maxIdx)) 0.3f else 1f
                            val nextVal = currentVal - (dragAmount * resistance)

                            scrollOffset.snapTo(nextVal)
                            onAction(VoiceRecorderAction.SeekTo((nextVal.coerceIn(0f, maxIdx) * millisPerBar).toLong()))
                        }
                    }
                },
                orientation = Orientation.Horizontal,
                onDragStopped = { velocity ->
                    if (!state.waveform.isRecording) {
                        val maxIdx = state.waveform.durationMillis.toFloat() / millisPerBar
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
                            onAction(VoiceRecorderAction.SeekTo((scrollOffset.value.coerceIn(0f, maxIdx) * millisPerBar).toLong()))
                        }
                    }
                }
            )
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val headX = widthPx / 2f

        Canvas(modifier = Modifier.fillMaxSize()) {
            val startX = headX - (scrollOffset.value * stepPx)
            val waveformHeight = heightPx - designSystem.dimen(DimenToken.HEADER_HEIGHT).toPx()
            val centerY = waveformHeight / 2f

            // Backgrounds
            drawRect(
                color = designSystem.color(ColorToken.BACKGROUND),
                size = Size(widthPx, waveformHeight)
            )

            val surfaceVariantColor: Color = designSystem.color(ColorToken.SURFACE_VARIANT)
            drawRect(
                color = surfaceVariantColor.copy(alpha = 0.5f),
                topLeft = Offset(startX, 0f),
                size = Size((state.waveform.durationMillis.toFloat() / millisPerBar) * stepPx, waveformHeight)
            )

            // Trim Highlight
            if (state.waveform.isTrimming) {
                val trimColor: Color = designSystem.color(state.trim.frameColor)
                val trimStartX = startX + (state.waveform.trimStartMillis.toFloat() / millisPerBar) * stepPx
                val trimEndX = startX + (state.waveform.trimEndMillis.toFloat() / millisPerBar) * stepPx
                drawRect(
                    color = trimColor.copy(alpha = 0.25f),
                    topLeft = Offset(trimStartX, 0f),
                    size = Size((trimEndX - trimStartX).coerceAtLeast(1f), waveformHeight)
                )
            }

            // Waveform Bars
            val firstVisible = ((-startX) / stepPx).toInt().coerceAtLeast(0)
            val lastVisible = ((widthPx - startX) / stepPx).toInt().coerceAtMost((state.waveform.durationMillis / millisPerBar).toInt())

            for (i in firstVisible..lastVisible) {
                val x = startX + (i * stepPx)
                val ampIdx = (i * millisPerBar / 33.33f).toInt()
                val amp = state.waveform.amplitudes.getOrElse(ampIdx) { 0.05f }
                val barH = (amp * waveformHeight * 0.55f).coerceAtLeast(designSystem.dimen(DimenToken.RECORDER_WAVEFORM_MIN_HEIGHT).toPx())

                drawLine(
                    color = designSystem.color(ColorToken.TEXT_PRIMARY),
                    start = Offset(x, centerY - barH / 2),
                    end = Offset(x, centerY + barH / 2),
                    strokeWidth = designSystem.dimen(DimenToken.RECORDER_WAVEFORM_BAR_WIDTH).toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Ruler
            val rulerY = waveformHeight
            val rulerTextColor = designSystem.color(ColorToken.RECORDER_RULER_TEXT)
            val dividerColor = designSystem.color(ColorToken.TEXT_PRIMARY).copy(alpha = 0.15f)

            drawLine(
                color = dividerColor,
                start = Offset(0f, rulerY),
                end = Offset(widthPx, rulerY),
                strokeWidth = designSystem.dimen(DimenToken.DIVIDER_THICKNESS).toPx()
            )

            val ticksPerSecond = 5
            val tickIntervalMillis = 1000f / ticksPerSecond
            val firstTick = (((scrollOffset.value * millisPerBar) - headX / stepPx * millisPerBar) / tickIntervalMillis).toInt().coerceAtLeast(0)
            val lastTick = (((scrollOffset.value * millisPerBar) + headX / stepPx * millisPerBar) / tickIntervalMillis).toInt()

            for (t in firstTick..lastTick) {
                val timeMillis = t * tickIntervalMillis
                val x = startX + (timeMillis / millisPerBar) * stepPx
                val isSecond = t % ticksPerSecond == 0
                val tickHeight = if (isSecond) {
                    designSystem.dimen(DimenToken.RECORDER_RULER_TICK_LARGE).toPx()
                } else {
                    designSystem.dimen(DimenToken.RECORDER_RULER_TICK_SMALL).toPx()
                }

                drawLine(
                    color = designSystem.color(ColorToken.TEXT_PRIMARY).copy(alpha = if (isSecond) 0.3f else 0.1f),
                    start = Offset(x, rulerY),
                    end = Offset(x, rulerY + tickHeight),
                    strokeWidth = designSystem.dimen(DimenToken.DIVIDER_THICKNESS).toPx()
                )

                if (isSecond) {
                    val sec = t / ticksPerSecond
                    val m = sec / 60
                    val s = sec % 60
                    val timeLabel = "$m:${if (s < 10) "0$s" else s}"
                    
                    val textLayoutResult = textMeasurer.measure(
                        text = timeLabel,
                        style = dataStyle.copy(color = rulerTextColor)
                    )
                    
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(
                            x - textLayoutResult.size.width / 2,
                            rulerY + designSystem.dimen(DimenToken.SPACING_SMALL).toPx()
                        )
                    )
                }
            }

            // Playhead
            val playheadColor = if (state.waveform.isRecording) {
                designSystem.color(state.waveform.primaryColor)
            } else {
                designSystem.color(state.waveform.secondaryColor)
            }
            drawLine(
                color = playheadColor,
                start = Offset(headX, 0f),
                end = Offset(headX, waveformHeight),
                strokeWidth = designSystem.dimen(DimenToken.RECORDER_WAVEFORM_BAR_WIDTH).toPx()
            )
            drawCircle(
                color = playheadColor,
                radius = (designSystem.dimen(DimenToken.RECORDER_WAVEFORM_BAR_WIDTH).toPx() * 1.75f),
                center = Offset(headX, 0f)
            )
            drawCircle(
                color = playheadColor,
                radius = (designSystem.dimen(DimenToken.RECORDER_WAVEFORM_BAR_WIDTH).toPx() * 1.75f),
                center = Offset(headX, waveformHeight)
            )
        }
    }
}
