package application.poligraf.ui.features.analyzer.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import application.poligraf.domain.model.MarkerShape
import application.poligraf.ui.foundation.models.AnalyzerMarker
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.IconToken
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlin.math.abs

@Composable
fun AnomalyTimeline(
    markers: List<AnalyzerMarker>,
    notes: List<application.poligraf.ui.foundation.models.SessionNoteUiModel> = emptyList(),
    currentDurationMillis: Long,
    seekPositionMillis: Long?,
    isPaused: Boolean,
    onSeek: (Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val designSystem = LocalDesignSystem.current
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val textMeasurer = rememberTextMeasurer()

    val textStyle = MaterialTheme.typography.labelSmall.copy(
        fontSize = 9.sp,
        color = designSystem.color(ColorToken.TEXT_SECONDARY).copy(alpha = 0.5f)
    )

    // Expanded scale: 40dp per second
    val dpPerSecond = 40.dp
    val pxPerMillis = with(density) { dpPerSecond.toPx() } / 1000f

    val scrollState = rememberScrollState()

    // Haptic feedback for ticks during manual scroll
    var lastVibratedSecond by remember { mutableStateOf(-1) }
    LaunchedEffect(isPaused) {
        if (isPaused) {
            snapshotFlow { scrollState.value }
                .collect { scrollValue ->
                    val currentSecond = ((scrollValue / pxPerMillis) / 1000).toInt()
                    if (currentSecond != lastVibratedSecond) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        lastVibratedSecond = currentSecond
                    }
                }
        }
    }

    val shapePainters = MarkerShape.entries.associateWith { shape ->
        val iconToken = when (shape) {
            MarkerShape.CIRCLE -> IconToken.SHAPE_CIRCLE
            MarkerShape.STAR -> IconToken.SHAPE_STAR
            MarkerShape.DIAMOND -> IconToken.SHAPE_DIAMOND
            MarkerShape.HEART -> IconToken.SHAPE_HEART
        }
        rememberVectorPainter(designSystem.icon(iconToken))
    }
    
    val notePainter = rememberVectorPainter(designSystem.icon(IconToken.HISTORY))

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            contentAlignment = Alignment.TopStart
        ) {
            val viewPortWidth = constraints.maxWidth.toFloat()
            val indicatorOffset = viewPortWidth / 2

            val durationPx = currentDurationMillis * pxPerMillis
            val totalContentWidthPx = indicatorOffset + durationPx + indicatorOffset
            val totalContentWidthDp = with(density) { totalContentWidthPx.toDp() }

            // 1. LIVE MODE: Follow the right edge with Throttling to prevent lags
            LaunchedEffect(currentDurationMillis, isPaused) {
                if (!isPaused) {
                    val targetScroll =
                        ((indicatorOffset + durationPx) - viewPortWidth).coerceAtLeast(0f).toInt()
                    // More aggressive throttling: only scroll if move > 20px
                    if (abs(scrollState.value - targetScroll) > 20) {
                        scrollState.scrollTo(targetScroll)
                    }
                }
            }

            // 2. PAUSE TRANSITION: When pausing, jump center indicator to the current head
            // In Review mode, we don't want to jump to the end automatically if we're already viewing.
            LaunchedEffect(isPaused) {
                if (isPaused && currentDurationMillis > 0) {
                    // Only auto-scroll to end if we just transitioned from Live
                    if (seekPositionMillis == null) {
                        val targetScroll = (currentDurationMillis * pxPerMillis).toInt()
                        scrollState.animateScrollTo(targetScroll.coerceIn(0, scrollState.maxValue))
                        onSeek(currentDurationMillis)
                    }
                } else if (!isPaused) {
                    onSeek(null)
                }
            }

            // 3. SEEK SYNC: Map whatever is under indicatorOffset to time
            // Only update VM if the scroll state is actually moving (manual scroll)
            LaunchedEffect(isPaused, currentDurationMillis) {
                if (isPaused) {
                    snapshotFlow { scrollState.value }
                        .distinctUntilChanged()
                        .map { scrollValue ->
                            ((scrollValue / pxPerMillis).toLong()).coerceIn(
                                0,
                                currentDurationMillis
                            )
                        }
                        .distinctUntilChanged()
                        .collect { time ->
                            // Avoid updating if it's already close to seekPositionMillis
                            if (seekPositionMillis == null || abs(time - seekPositionMillis) > 50) {
                                onSeek(time)
                            }
                        }
                }
            }

            // Handle external seek position changes (e.g. restoration or marker click)
            LaunchedEffect(seekPositionMillis) {
                if (isPaused && seekPositionMillis != null) {
                    val targetScroll = (seekPositionMillis * pxPerMillis).toInt()
                    // Increase threshold to avoid jitter loops
                    if (abs(targetScroll - scrollState.value) > 5) {
                        scrollState.scrollTo(targetScroll.coerceIn(0, scrollState.maxValue))
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Background Track (Static across full width of the screen)
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                ) {
                    drawRoundRect(
                        color = designSystem.color(ColorToken.SURFACE_BACKGROUND)
                            .copy(alpha = 0.3f),
                        topLeft = Offset(0f, 0f),
                        size = Size(size.width, size.height),
                        cornerRadius = CornerRadius(6.dp.toPx())
                    )
                }

                // Scrollable Content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(
                            state = scrollState,
                            enabled = isPaused
                        )
                ) {
                    val visibleRange by remember {
                        derivedStateOf {
                            val start = scrollState.value.toFloat()
                            val end = start + viewPortWidth
                            start to end
                        }
                    }

                    Canvas(
                        modifier = Modifier
                            .width(totalContentWidthDp)
                            .fillMaxHeight()
                    ) {
                        val stripHeight = 36.dp.toPx()
                        val stripTop = 0f
                        val stripCenterY = stripTop + stripHeight / 2

                        val (visibleStartPx, visibleEndPx) = visibleRange

                        // 1. Magnetic Notches Background (OPTIMIZED: only draw visible)
                        val notchStep = 4.dp.toPx()
                        val firstVisibleNotch = (visibleStartPx / notchStep).toInt()
                        val lastVisibleNotch = (visibleEndPx / notchStep).toInt()
                        
                        for (i in firstVisibleNotch..lastVisibleNotch) {
                            val nx = i * notchStep
                            drawLine(
                                color = designSystem.color(ColorToken.SURFACE_VARIANT)
                                    .copy(alpha = 0.05f),
                                start = Offset(nx, stripTop + 4.dp.toPx()),
                                end = Offset(nx, stripTop + stripHeight - 4.dp.toPx()),
                                strokeWidth = 0.5.dp.toPx()
                            )
                        }

                        // 2. Active Recorded Background Strip
                        if (durationPx > 0) {
                            drawRoundRect(
                                color = designSystem.color(ColorToken.SURFACE_VARIANT)
                                    .copy(alpha = 0.16f),
                                topLeft = Offset(indicatorOffset, stripTop),
                                size = Size(durationPx, stripHeight),
                                cornerRadius = CornerRadius(6.dp.toPx())
                            )
                        }

                        // Ticks and Labels (Optimized: only draw what fits on screen)
                        val firstVisibleSecond =
                            (((visibleStartPx - indicatorOffset) / (1000f * pxPerMillis)).toInt() - 1).coerceAtLeast(
                                0
                            )
                        val lastVisibleSecond =
                            (((visibleEndPx - indicatorOffset) / (1000f * pxPerMillis)).toInt() + 1)
                                .coerceAtMost((currentDurationMillis / 1000).toInt())

                        if (firstVisibleSecond <= lastVisibleSecond) {
                            for (s in firstVisibleSecond..lastVisibleSecond) {
                                val x = indicatorOffset + (s * 1000f * pxPerMillis)
                                val isMajor = s % 2 == 0
                                val isLabeled =
                                    s % 2 == 0 // Labeled every 2s for increased readability at 40dp/s

                                val tickStartY = stripTop + stripHeight + 6.dp.toPx()
                                val tickHeight = if (isMajor) 8.dp.toPx() else 4.dp.toPx()

                                drawLine(
                                    color = designSystem.color(ColorToken.TEXT_SECONDARY)
                                        .copy(alpha = if (isMajor) 0.35f else 0.12f),
                                    start = Offset(x, tickStartY),
                                    end = Offset(x, tickStartY + tickHeight),
                                    strokeWidth = 1.dp.toPx()
                                )

                                if (isLabeled) {
                                    val timeStr = "${
                                        (s / 60).toString().padStart(2, '0')
                                    }:${(s % 60).toString().padStart(2, '0')}"
                                    val textLayout = textMeasurer.measure(timeStr, textStyle)
                                    drawText(
                                        textLayout,
                                        topLeft = Offset(
                                            x - textLayout.size.width / 2,
                                            tickStartY + tickHeight + 2.dp.toPx()
                                        )
                                    )
                                }
                            }
                        }

                        // Markers (Only visible markers)
                        markers.forEach { marker ->
                            val x = indicatorOffset + (marker.timestampMillis * pxPerMillis)
                            if (x in (visibleStartPx - 15f)..(visibleEndPx + 15f)) {
                                shapePainters[marker.shape]?.let { painter ->
                                    drawMarker(
                                        painter = painter,
                                        color = designSystem.color(marker.colorToken),
                                        center = Offset(x, stripCenterY),
                                        size = 14.dp.toPx(),
                                        outlineColor = designSystem.color(ColorToken.SURFACE_BACKGROUND)
                                    )
                                }
                            }
                        }
                        
                        // Note Markers (Only visible notes)
                        notes.forEach { note ->
                            val x = indicatorOffset + (note.timestampMillis * pxPerMillis)
                            if (x in (visibleStartPx - 15f)..(visibleEndPx + 15f)) {
                                drawMarker(
                                    painter = notePainter,
                                    color = designSystem.color(ColorToken.ACCENT_PRIMARY),
                                    center = Offset(x, stripCenterY),
                                    size = 12.dp.toPx(),
                                    outlineColor = designSystem.color(ColorToken.SURFACE_BACKGROUND)
                                )
                            }
                        }
                    }
                }

                // Pointer Overlay (Real-time live position or paused center indicator)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stripHeight = 36.dp.toPx()

                    // In Live mode: pointer follows the analysis head on screen
                    // In Paused mode: pointer is centered and colored in accent
                    val pointerX = if (isPaused) {
                        indicatorOffset
                    } else {
                        val currentScreenPos = (indicatorOffset + durationPx) - scrollState.value
                        currentScreenPos.coerceIn(0f, viewPortWidth)
                    }

                    val pointerColor = if (isPaused) {
                        designSystem.color(ColorToken.ACCENT_PRIMARY)
                    } else {
                        designSystem.color(ColorToken.TEXT_SECONDARY).copy(alpha = 0.5f)
                    }

                    drawLine(
                        color = pointerColor,
                        start = Offset(pointerX, -4f),
                        end = Offset(pointerX, stripHeight + 4f),
                        strokeWidth = 2.dp.toPx()
                    )
                    drawCircle(
                        color = pointerColor,
                        radius = 4.dp.toPx(),
                        center = Offset(pointerX, stripHeight / 2)
                    )
                }
            }
        }
    }
}

fun DrawScope.drawMarker(
    painter: Painter,
    color: Color,
    center: Offset,
    size: Float,
    outlineColor: Color? = null,
) {
    if (outlineColor != null) {
        val outlineSize = size + 2.dp.toPx()
        translate((center.x - outlineSize / 2), (center.y - outlineSize / 2)) {
            with(painter) {
                draw(
                    size = Size(outlineSize, outlineSize),
                    colorFilter = ColorFilter.tint(outlineColor),
                )
            }
        }
    }
    translate((center.x - size / 2), (center.y - size / 2)) {
        with(painter) {
            draw(
                size = Size(size, size),
                colorFilter = ColorFilter.tint(color),
            )
        }
    }
}
