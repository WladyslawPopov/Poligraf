package application.poligraf.ui.features.analyzer.visualizations

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.StringToken
import kotlin.math.sin

@Composable
fun VoiceRibbonVisualization(
    jitterLevel: Float,
    pitchLevel: Float,
    rmsLevel: Float,
    isPaused: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ribbon")
    
    // Only animate phase when actively analyzing
    val time by if (!isPaused) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 6.28f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ), label = "time"
        )
    } else {
        remember { mutableStateOf(0f) }
    }
    
    val designSystem = LocalDesignSystem.current

    Box(
        modifier = modifier.fillMaxWidth().height(220.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerY = height / 2
            
            val syncZoneStart = width * 0.35f
            val syncZoneWidth = width * 0.3f
            
            // Sync Zone Indicator (Rectangle zone from mockups)
            drawRoundRect(
                color = designSystem.color(ColorToken.SURFACE_VARIANT).copy(alpha = 0.12f),
                topLeft = Offset(syncZoneStart, 16.dp.toPx()),
                size = Size(syncZoneWidth, height - 32.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
            )

            val factors = listOf(pitchLevel, rmsLevel, jitterLevel)
            val colors = listOf(
                designSystem.color(ColorToken.CHART_PITCH),
                designSystem.color(ColorToken.CHART_RMS),
                designSystem.color(ColorToken.CHART_JITTER)
            )
            
            factors.forEachIndexed { index, factor ->
                val path = Path()
                // Smooth sine waves - Boosted amplitude for Instrument 2.7
                val amplitude = (height / 2.2f) * factor.coerceIn(0.12f, 1f)
                val freq = 6f
                
                for (x in 0..width.toInt() step 4) {
                    val xRatio = x / width
                    val wave = sin((xRatio * freq) + time + (index * 2.1f)) * amplitude
                    if (x == 0) path.moveTo(x.toFloat(), centerY + wave)
                    else path.lineTo(x.toFloat(), centerY + wave)
                }
                
                drawPath(
                    path = path,
                    color = colors[index].copy(alpha = 0.9f),
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }

        // Zone label above/below sync zone
        Text(
            text = designSystem.string(StringToken.LABEL_SYNC_ZONE),
            color = designSystem.color(ColorToken.TEXT_SECONDARY).copy(alpha = 0.4f),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp)
        )
    }
}
