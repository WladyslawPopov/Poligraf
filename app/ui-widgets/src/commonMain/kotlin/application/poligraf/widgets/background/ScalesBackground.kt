package application.liedetector.widgets.background

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.blur
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import application.liedetector.uicore.theme.tokens.DimenToken
import application.liedetector.uicore.theme.LocalDesignSystem
import application.liedetector.uicore.widgets.AppBackground
import application.liedetector.uicore.theme.tokens.ColorToken
import application.liedetector.uicore.types.BackgroundMode
import application.liedetector.widgets.utils.composeColor
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sin

@Composable
fun ScalesBackground(
    config: AppBackground.AnimatedScales,
    modifier: Modifier = Modifier
) {
    val designSystem = LocalDesignSystem.current
    
    val (tiltX, tiltY) = rememberTiltState()
    
    val smoothX by animateFloatAsState(
        targetValue = tiltX,
        animationSpec = spring(stiffness = 100f, dampingRatio = Spring.DampingRatioLowBouncy)
    )
    val smoothY by animateFloatAsState(
        targetValue = tiltY,
        animationSpec = spring(stiffness = 100f, dampingRatio = Spring.DampingRatioLowBouncy)
    )

    val speedMultiplier = when (config.mode) {
        BackgroundMode.PROCESSING -> 5.0f
        BackgroundMode.RECORDING -> 0.4f
        BackgroundMode.ERROR -> 0.2f
        BackgroundMode.SUCCESS -> 1.5f
        else -> 1.0f
    }

    val infiniteTransition = rememberInfiniteTransition(label = "aura")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween((8000 / (config.animationSpeed * speedMultiplier)).toInt(), easing = LinearEasing)
        )
    )

    val pulseScale by if (config.mode == BackgroundMode.RECORDING) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    val baseBgColor: Color = designSystem.composeColor(config.baseColor)
    val scaleColor: Color = designSystem.composeColor(config.particleColor)
    
    val energyColorToken = when (config.mode) {
        BackgroundMode.ERROR -> ColorToken.ERROR
        BackgroundMode.SUCCESS -> ColorToken.TRUTH
        BackgroundMode.RECORDING -> ColorToken.ACCENT_ENERGY
        BackgroundMode.PROCESSING -> ColorToken.WARNING
        BackgroundMode.WAITING -> ColorToken.ACCENT_ENERGY
        else -> config.energyColor
    }
    val energyColor by animateColorAsState(
        targetValue = designSystem.composeColor(energyColorToken),
        animationSpec = tween(400)
    )

    val truthColor: Color = designSystem.composeColor(ColorToken.TRUTH)
    val stressColor: Color = designSystem.composeColor(ColorToken.STRESS)
    val recordingBaseColor: Color = designSystem.composeColor(ColorToken.ACCENT_ENERGY)

    val blurRadius by animateFloatAsState(
        targetValue = if (config.mode == BackgroundMode.ERROR) config.blurRadius * 1.5f else config.blurRadius,
        animationSpec = tween(600)
    )
    
    val baseParallax = designSystem.dimen(DimenToken.PARALLAX_INTENSITY)
    val parallaxIntensity = baseParallax * config.parallaxIntensity
    val density = LocalDensity.current
    val cornerRadiusPx = with(density) { designSystem.dimen(DimenToken.CORNER_RADIUS).dp.toPx() }
    
    val cellWidthPx = with(density) { designSystem.dimen(DimenToken.BACKGROUND_CELL_WIDTH).dp.toPx() }
    val cellHeightPx = with(density) { designSystem.dimen(DimenToken.BACKGROUND_CELL_HEIGHT).dp.toPx() }

    Box(modifier = modifier.fillMaxSize().background(baseBgColor)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .blur(blurRadius.dp)
        ) {
            val cols = (size.width / cellWidthPx).toInt()
            val rows = (size.height / cellHeightPx).toInt()
            
            val cellW = size.width / cols
            val cellH = size.height / rows
            val px = smoothX * parallaxIntensity
            val py = smoothY * parallaxIntensity

            val rectSize = Size(
                (cellW / 1.5f) * pulseScale, 
                (cellH / 2.0f) * pulseScale
            )
            val halfRectW = rectSize.width / 2
            val halfRectH = rectSize.height / 2

            // Expanded loop range to prevent dark edges during parallax
            for (r in -2..rows + 2) {
                val y = r * cellH + cellH / 2 + py
                val dyNorm = abs(y - size.height / 2) / (size.height * 0.5f)
                val dy4 = dyNorm * dyNorm * dyNorm * dyNorm

                for (c in -2..cols + 2) {
                    val x = c * cellW + cellW / 2 + px
                    val dxNorm = abs(x - size.width / 2) / (size.width * 0.5f)
                    val distMask = (dxNorm * dxNorm * dxNorm * dxNorm + dy4).pow(0.25f)
                    
                    val wave = sin(distMask * 10f - time).coerceIn(0f, 1f)
                    val energyIntensity = (distMask * 1.2f).coerceIn(0.2f, 1f)
                    val rectTopLeft = Offset(x - halfRectW, y - halfRectH)

                    val finalEnergyColor = when (config.mode) {
                        BackgroundMode.RECORDING -> {
                            // Red pulse every few seconds based on distance and time
                            val pulse = (sin(distMask * 5f - time * 1.5f) * 0.5f + 0.5f).pow(10f)
                            lerp(recordingBaseColor, stressColor, pulse.coerceIn(0f, 1f))
                        }
                        BackgroundMode.WAITING -> {
                            val mix = (sin(x * 0.01f + time) * 0.5f + 0.5f)
                            lerp(truthColor, stressColor, mix)
                        }
                        else -> energyColor
                    }

                    // Draw Scale
                    drawRoundRect(
                        color = scaleColor,
                        topLeft = rectTopLeft,
                        size = rectSize,
                        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                        alpha = 0.3f
                    )

                    // Draw Energy
                    drawRoundRect(
                        color = finalEnergyColor.copy(alpha = energyIntensity * 0.4f * (0.3f + wave * 0.7f)),
                        topLeft = rectTopLeft,
                        size = rectSize,
                        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                    )
                }
            }
        }
    }
}
