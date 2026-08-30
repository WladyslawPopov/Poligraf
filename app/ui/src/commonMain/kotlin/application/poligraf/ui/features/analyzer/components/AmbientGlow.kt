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
    modifier: Modifier = Modifier
) {
    val designSystem = LocalDesignSystem.current
    val visible = signalLevel != SignalLevel.NONE

    val anomalyColor = remember(signalLevel, dominantMetric) {
        when (dominantMetric) {
            DominantMetric.JITTER -> designSystem.color(ColorToken.CHART_JITTER)
            DominantMetric.PITCH -> designSystem.color(ColorToken.CHART_PITCH)
            DominantMetric.RMS -> designSystem.color(ColorToken.CHART_RMS)
            null -> designSystem.color(ColorToken.CHART_JITTER)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "ambient")
    val ambientAlpha by infiniteTransition.animateFloat(
        initialValue = 0.10f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "alpha"
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(600, easing = LinearOutSlowInEasing)),
        exit = fadeOut(tween(800, easing = LinearOutSlowInEasing)),
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
                                anomalyColor.copy(alpha = ambientAlpha * 1.4f),
                                anomalyColor.copy(alpha = ambientAlpha * 0.6f),
                                Color.Transparent
                            ),
                            center = Offset(width / 2f, 0f),
                            radius = 1800f
                        )
                    )
            )
        }
    }
}
