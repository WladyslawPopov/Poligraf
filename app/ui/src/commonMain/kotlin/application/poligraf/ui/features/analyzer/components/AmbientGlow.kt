package application.poligraf.ui.features.analyzer.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import application.poligraf.engine.dsp.DominantMetric
import application.poligraf.engine.dsp.SignalLevel
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken

@Composable
fun AmbientGlow(
    signalLevel: SignalLevel,
    dominantMetric: DominantMetric?,
    jitterLevel: Float,
    pitchLevel: Float,
    rmsLevel: Float,
    modifier: Modifier = Modifier
) {
    val designSystem = LocalDesignSystem.current
    
    // UI Sensitivity: Even low levels should create a subtle glow
    val maxLevel = maxOf(jitterLevel, pitchLevel, rmsLevel)
    val visible = maxLevel > 0.02f

    val anomalyColor = remember(signalLevel, dominantMetric) {
        when (dominantMetric) {
            DominantMetric.JITTER -> designSystem.color(ColorToken.CHART_JITTER)
            DominantMetric.PITCH -> designSystem.color(ColorToken.CHART_PITCH)
            DominantMetric.RMS -> designSystem.color(ColorToken.CHART_RMS)
            null -> designSystem.color(ColorToken.CHART_JITTER)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "ambient")
    val breathingBase by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "breathing"
    )

    // Modulate alpha by the actual signal intensity
    // UI Sensitivity: Faster scaling for subtle half-tones
    val intensityAlpha = (maxLevel * 0.5f).coerceAtLeast(breathingBase)

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400, easing = LinearOutSlowInEasing)),
        exit = fadeOut(tween(1200, easing = LinearOutSlowInEasing)),
        modifier = modifier
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val width = constraints.maxWidth.toFloat()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                anomalyColor.copy(alpha = intensityAlpha * 1.5f),
                                anomalyColor.copy(alpha = intensityAlpha * 0.4f),
                                Color.Transparent
                            ),
                            center = Offset(width / 2f, 0f),
                            radius = (1000f + maxLevel * 1200f) // Radius also plays with intensity
                        )
                    )
            )
        }
    }
}
