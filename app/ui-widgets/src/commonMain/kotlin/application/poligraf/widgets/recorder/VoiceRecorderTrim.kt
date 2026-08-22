package application.poligraf.widgets.recorder

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import application.poligraf.uicore.state.VoiceRecorderAction
import application.poligraf.uicore.state.VoiceRecorderUiState
import application.poligraf.uicore.theme.DesignSystem
import application.poligraf.uicore.theme.tokens.ColorToken
import application.poligraf.uicore.theme.tokens.DimenToken
import application.poligraf.uicore.theme.tokens.IconToken
import application.poligraf.widgets.AppIcon
import kotlin.math.absoluteValue

@Composable
fun MiniTrimOverview(
    state: VoiceRecorderUiState,
    onAction: (VoiceRecorderAction) -> Unit,
    designSystem: DesignSystem
) {
    var localStart by remember { mutableLongStateOf(state.trim.startMillis) }
    var localEnd by remember { mutableLongStateOf(state.trim.endMillis) }
    var isDragging by remember { mutableStateOf(false) }

    val isVisible = state.trim.isVisible
    LaunchedEffect(isVisible) {
        if (isVisible) {
            localStart = state.trim.startMillis
            localEnd = state.trim.endMillis
        }
    }

    LaunchedEffect(state.trim.startMillis, state.trim.endMillis, isDragging) {
        if (!isDragging) {
            localStart = state.trim.startMillis
            localEnd = state.trim.endMillis
        }
    }

    val sidePadding = 32.dp
    BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).height(56.dp)) {
        val widthPx = constraints.maxWidth.toFloat()
        val sidePaddingPx = with(LocalDensity.current) { sidePadding.toPx() }
        val trackWidthPx = widthPx - 2 * sidePaddingPx

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = sidePadding)
                .clip(RoundedCornerShape(designSystem.dimen(DimenToken.SPACING_TINY)))
                .background(designSystem.color(ColorToken.BACKGROUND).copy(alpha = 0.3f))
                .pointerInput(state.waveform.durationMillis) {
                    detectTapGestures { offset ->
                        val tappedTime = ((offset.x - sidePaddingPx).coerceIn(0f, trackWidthPx) / trackWidthPx * state.waveform.durationMillis).toLong()
                        if ((tappedTime - localStart).absoluteValue < (tappedTime - localEnd).absoluteValue) {
                            localStart = tappedTime.coerceIn(0, localEnd - 100)
                        } else {
                            localEnd = tappedTime.coerceIn(localStart + 100, state.waveform.durationMillis)
                        }
                        onAction(VoiceRecorderAction.UpdateTrimRange(localStart, localEnd))
                    }
                }
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val step = 2.dp.toPx()
                val count = (size.width / step).toInt()
                if (state.waveform.amplitudes.isNotEmpty()) {
                    for (i in 0 until count) {
                        val x = i * step
                        val amp = state.waveform.amplitudes.getOrNull((i.toFloat() / count * state.waveform.amplitudes.size).toInt()) ?: 0.1f
                        val h = (amp * size.height * 0.4f).coerceAtLeast(1.dp.toPx())
                        drawRoundRect(
                            color = designSystem.color(ColorToken.TEXT_PRIMARY).copy(alpha = 0.2f),
                            topLeft = Offset(x, size.height/2 - h/2),
                            size = Size(1.dp.toPx(), h),
                            cornerRadius = CornerRadius(0.5.dp.toPx())
                        )
                    }
                }
            }
        }

        val startRatio = localStart.toFloat() / state.waveform.durationMillis.coerceAtLeast(1)
        val endRatio = localEnd.toFloat() / state.waveform.durationMillis.coerceAtLeast(1)
        val playbackRatio = state.waveform.playbackPositionMillis.toFloat() / state.waveform.durationMillis.coerceAtLeast(1)

        val leftX = sidePaddingPx + (trackWidthPx * startRatio)
        val rightX = sidePaddingPx + (trackWidthPx * endRatio)
        val playheadX = sidePaddingPx + (trackWidthPx * playbackRatio)

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(with(LocalDensity.current) { (rightX - leftX).toDp() })
                .offset(x = with(LocalDensity.current) { leftX.toDp() })
                .border(2.dp, designSystem.color(state.trim.frameColor))
        )

        TrimHandle(
            x = leftX,
            isLeft = true,
            currentMillis = localStart,
            otherMillis = localEnd,
            durationMillis = state.waveform.durationMillis,
            trackWidthPx = trackWidthPx,
            onDragStateChange = { isDragging = it },
            onUpdate = { newStart ->
                localStart = newStart
                onAction(VoiceRecorderAction.UpdateTrimRange(localStart, localEnd))
            },
            designSystem = designSystem,
            frameColor = state.trim.frameColor,
            icon = state.trim.handleIconLeft
        )

        TrimHandle(
            x = rightX,
            isLeft = false,
            currentMillis = localEnd,
            otherMillis = localStart,
            durationMillis = state.waveform.durationMillis,
            trackWidthPx = trackWidthPx,
            onDragStateChange = { isDragging = it },
            onUpdate = { newEnd ->
                localEnd = newEnd
                onAction(VoiceRecorderAction.UpdateTrimRange(localStart, localEnd))
            },
            designSystem = designSystem,
            frameColor = state.trim.frameColor,
            icon = state.trim.handleIconRight
        )

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(2.dp)
                .offset(x = with(LocalDensity.current) { (playheadX - 1.dp.toPx()).toDp() })
                .background(designSystem.color(state.waveform.secondaryColor))
        )
    }
}

@Composable
private fun TrimHandle(
    x: Float,
    isLeft: Boolean,
    currentMillis: Long,
    otherMillis: Long,
    durationMillis: Long,
    trackWidthPx: Float,
    onDragStateChange: (Boolean) -> Unit,
    onUpdate: (Long) -> Unit,
    designSystem: DesignSystem,
    frameColor: ColorToken,
    icon: IconToken
) {
    val currentMillisState = rememberUpdatedState(currentMillis)
    val otherMillisState = rememberUpdatedState(otherMillis)
    val durationMillisState = rememberUpdatedState(durationMillis)
    val trackWidthPxState = rememberUpdatedState(trackWidthPx)

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(designSystem.dimen(DimenToken.AVATAR_SIZE_SMALL))
            .offset(x = with(LocalDensity.current) { (x - (if (isLeft) 34.dp.toPx() else 14.dp.toPx())).toDp() })
            .pointerInput(Unit) {
                var accumulatedDrag = 0f
                detectDragGestures(
                    onDragStart = {
                        accumulatedDrag = 0f
                        onDragStateChange(true)
                    },
                    onDragEnd = { onDragStateChange(false) },
                    onDragCancel = { onDragStateChange(false) },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        accumulatedDrag += dragAmount.x

                        val trackWidth = trackWidthPxState.value
                        val duration = durationMillisState.value

                        if (trackWidth > 0) {
                            val deltaMillis = (accumulatedDrag / trackWidth * duration).toLong()
                            if (deltaMillis != 0L) {
                                val newValue = if (isLeft) {
                                    (currentMillisState.value + deltaMillis).coerceIn(0, otherMillisState.value - 100)
                                } else {
                                    (currentMillisState.value + deltaMillis).coerceIn(otherMillisState.value + 100, duration)
                                }

                                if (newValue != currentMillisState.value) {
                                    onUpdate(newValue)
                                    accumulatedDrag = 0f
                                }
                            }
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(20.dp)
                .background(
                    designSystem.color(frameColor),
                    RoundedCornerShape(
                        topStart = if (isLeft) 4.dp else 0.dp,
                        bottomStart = if (isLeft) 4.dp else 0.dp,
                        topEnd = if (!isLeft) 4.dp else 0.dp,
                        bottomEnd = if (!isLeft) 4.dp else 0.dp
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(
                icon = designSystem.icon(icon),
                contentDescription = null,
                tint = designSystem.color(ColorToken.TEXT_INVERTED),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
