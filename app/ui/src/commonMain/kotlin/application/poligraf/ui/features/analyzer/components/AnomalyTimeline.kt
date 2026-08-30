package application.poligraf.ui.features.analyzer.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
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
import androidx.compose.ui.platform.LocalDensity
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

@Composable
fun AnomalyTimeline(
    markers: List<AnalyzerMarker>,
    currentDurationMillis: Long,
    seekPositionMillis: Long?,
    isPaused: Boolean,
    onSeek: (Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val designSystem = LocalDesignSystem.current
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    val textStyle = MaterialTheme.typography.labelSmall.copy(
        fontSize = 8.sp,
        color = designSystem.color(ColorToken.TEXT_SECONDARY).copy(alpha = 0.4f)
    )

    val dpPerSecond = 20.dp
    val pxPerMillis = with(density) { dpPerSecond.toPx() } / 1000f

    val scrollState = rememberScrollState()

    val shapePainters = MarkerShape.entries.associateWith { shape ->
        val iconToken = when (shape) {
            MarkerShape.CIRCLE -> IconToken.SHAPE_CIRCLE
            MarkerShape.STAR -> IconToken.SHAPE_STAR
            MarkerShape.DIAMOND -> IconToken.SHAPE_DIAMOND
            MarkerShape.HEART -> IconToken.SHAPE_HEART
        }
        rememberVectorPainter(designSystem.icon(iconToken))
    }

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

            // 1. LIVE MODE: Follow the right edge of the viewport
            LaunchedEffect(currentDurationMillis, isPaused) {
                if (!isPaused) {
                    val targetScroll =
                        ((indicatorOffset + durationPx) - viewPortWidth).coerceAtLeast(0f).toInt()
                    scrollState.scrollTo(targetScroll)
                }
            }

            // 2. PAUSE TRANSITION: When pausing, jump center indicator to the end of analysis
            LaunchedEffect(isPaused) {
                if (isPaused) {
                    val targetScroll = durationPx.toInt()
                    scrollState.scrollTo(targetScroll.coerceIn(0, scrollState.maxValue))
                    onSeek(currentDurationMillis)
                } else {
                    onSeek(null)
                }
            }

            // 3. SEEK SYNC: Map whatever is under indicatorOffset to time
            LaunchedEffect(isPaused, currentDurationMillis) {
                if (isPaused) {
                    snapshotFlow { scrollState.value }
                        .map { scrollValue ->
                            ((scrollValue / pxPerMillis).toLong()).coerceIn(
                                0,
                                currentDurationMillis
                            )
                        }
                        .distinctUntilChanged()
                        .collect { time ->
                            onSeek(time)
                        }
                }
            }

            // Handle external seek position changes (e.g. restoration)
            LaunchedEffect(seekPositionMillis) {
                if (isPaused && seekPositionMillis != null) {
                    val targetScroll = (seekPositionMillis * pxPerMillis).toInt()
                    if (kotlin.math.abs(targetScroll - scrollState.value) > 2) {
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
                        .horizontalScroll(scrollState, enabled = isPaused)
                ) {
                    Canvas(
                        modifier = Modifier
                            .width(totalContentWidthDp)
                            .fillMaxHeight()
                    ) {
                        val stripHeight = 36.dp.toPx()
                        val stripTop = 0f
                        val stripCenterY = stripTop + stripHeight / 2

                        // Active Recorded Background Strip
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
                        val visibleStartPx = scrollState.value.toFloat()
                        val visibleEndPx = visibleStartPx + viewPortWidth

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
                                val isLabeled = s % 5 == 0

                                val tickStartY = stripTop + stripHeight + 6.dp.toPx()
                                val tickHeight = if (isMajor) 8.dp.toPx() else 4.dp.toPx()

                                drawLine(
                                    color = designSystem.color(ColorToken.TEXT_SECONDARY)
                                        .copy(alpha = if (isMajor) 0.25f else 0.08f),
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
                                val painter = shapePainters[marker.shape]
                                if (painter != null) {
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
        translate(center.x - outlineSize / 2, center.y - outlineSize / 2) {
            with(painter) {
                draw(
                    size = Size(outlineSize, outlineSize),
                    colorFilter = ColorFilter.tint(outlineColor)
                )
            }
        }
    }
    translate(center.x - size / 2, center.y - size / 2) {
        with(painter) {
            draw(
                size = Size(size, size),
                colorFilter = ColorFilter.tint(color)
            )
        }
    }
}
