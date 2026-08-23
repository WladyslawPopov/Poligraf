package application.poligraf.ui.features.render

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import application.poligraf.ui.foundation.actions.WidgetAction
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.DimenToken
import application.poligraf.ui.theme.tokens.IconToken
import application.poligraf.ui.foundation.models.UiWidget

@Composable
fun AnalyzeBtnRenderer(
    widget: UiWidget.AnalyzeBtn,
    onAction: (WidgetAction) -> Unit
){
    val designSystem = LocalDesignSystem.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Cache colors and dimensions
    val accentPrimary = remember(designSystem.isDark) { designSystem.color(ColorToken.ACCENT_PRIMARY) }
    val accentEnergy = remember(designSystem.isDark) { designSystem.color(ColorToken.ACCENT_ENERGY) }
    val surfaceColor = remember(designSystem.isDark) { designSystem.color(ColorToken.SURFACE_PRIMARY) }
    val bgColor = remember(designSystem.isDark) { designSystem.color(ColorToken.SURFACE_BACKGROUND) }
    val btnSize = designSystem.dimen(DimenToken.RECORDER_BTN_SIZE)
    val strokeWidthDp = designSystem.dimen(DimenToken.RECORDER_BTN_STROKE)
    val spacing = designSystem.dimen(DimenToken.SPACING_LARGE)

    val infiniteTransition = rememberInfiniteTransition(label = "hypnotic")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotation"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(stiffness = 200f, dampingRatio = Spring.DampingRatioLowBouncy),
        label = "scale"
    )

    // Pre-calculate brushes
    val sweepBrush = remember(accentPrimary, accentEnergy) {
        Brush.sweepGradient(
            colors = listOf(
                accentPrimary.copy(alpha = 0.0f),
                accentPrimary.copy(alpha = 0.8f),
                accentEnergy.copy(alpha = 0.0f),
                accentPrimary.copy(alpha = 0.4f),
                accentPrimary.copy(alpha = 0.0f)
            )
        )
    }

    val orbBrush = remember(surfaceColor, bgColor) {
        Brush.radialGradient(
            colors = listOf(
                surfaceColor.copy(alpha = 0.2f),
                bgColor.copy(alpha = 0.8f)
            )
        )
    }

    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = spacing),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(btnSize)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { onAction(widget.action) }
                ),
            contentAlignment = Alignment.Center
        ) {
            // 1. Cyber-Sweeping Gradient (Rotated via graphicsLayer)
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationZ = rotation }
            ) {
                drawArc(
                    brush = sweepBrush,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidthDp.toPx())
                )
            }

            // 2. Futuristic Orb Base
            Box(
                modifier = Modifier
                    .fillMaxSize(0.75f)
                    .clip(CircleShape)
                    .background(brush = orbBrush)
                    .drawBehind {
                        // Core Glow (Animated via pulse)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    accentPrimary.copy(alpha = 0.3f * pulse),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = size.minDimension / 1.2f
                            )
                        )

                        // Scanning line (Now correctly uses parent size)
                        val scanY = ((pulse - 0.8f) / 0.4f) * size.height
                        drawLine(
                            color = accentPrimary.copy(alpha = 0.25f),
                            start = Offset(0f, scanY),
                            end = Offset(size.width, scanY),
                            strokeWidth = 2.dp.toPx()
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // 3. Central Icon (Animated via graphicsLayer)
                Icon(
                    imageVector = designSystem.icon(IconToken.MIC),
                    contentDescription = null,
                    tint = accentPrimary,
                    modifier = Modifier
                        .size(btnSize * 0.3f)
                        .graphicsLayer {
                            val iconPulse = 0.7f + (pulse * 0.3f)
                            alpha = iconPulse
                            scaleX = 0.9f + (pulse * 0.1f)
                            scaleY = 0.9f + (pulse * 0.1f)
                        }
                )
            }
        }
    }
}
