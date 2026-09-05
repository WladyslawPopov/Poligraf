package application.poligraf.ui.features.analyzer.visualizations

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken

/**
 * Minimalist Barycentric State Map Triangle.
 * Vertices are clean colored dots (Pitch = Blue, RMS = Orange, Jitter = Green)
 * without text clutter, delegating metric labels to [MetricRow].
 */
@Composable
fun StateMapVisualization(
    jitterLevel: Float, 
    pitchLevel: Float, 
    rmsLevel: Float,
    modifier: Modifier = Modifier
) {
    val designSystem = LocalDesignSystem.current

    Box(modifier = modifier.fillMaxWidth().height(260.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(240.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = (size.minDimension / 2f) - 16.dp.toPx()
            
            // Vertices exactly aligned with triangle corners
            // Top: Pitch (Blue)
            val pPitch = Offset(center.x, center.y - radius) 
            // Bottom-Left: RMS (Orange)
            val pRms = Offset(center.x - radius * 0.866f, center.y + radius * 0.5f) 
            // Bottom-Right: Jitter (Green)
            val pJitter = Offset(center.x + radius * 0.866f, center.y + radius * 0.5f) 
            
            val trianglePath = Path().apply {
                moveTo(pPitch.x, pPitch.y)
                lineTo(pRms.x, pRms.y)
                lineTo(pJitter.x, pJitter.y)
                close()
            }
            
            // Dynamic Gradient Mesh based on vertex colors (Pitch = Blue, RMS = Orange, Jitter = Green)
            drawPath(
                path = trianglePath,
                brush = Brush.radialGradient(
                    0.0f to designSystem.color(ColorToken.CHART_JITTER).copy(alpha = (jitterLevel * 0.65f).coerceIn(0.08f, 0.7f)),
                    1.0f to Color.Transparent,
                    center = pJitter,
                    radius = radius * 1.4f
                )
            )
            drawPath(
                path = trianglePath,
                brush = Brush.radialGradient(
                    0.0f to designSystem.color(ColorToken.CHART_PITCH).copy(alpha = (pitchLevel * 0.65f).coerceIn(0.08f, 0.7f)),
                    1.0f to Color.Transparent,
                    center = pPitch,
                    radius = radius * 1.4f
                )
            )
            drawPath(
                path = trianglePath,
                brush = Brush.radialGradient(
                    0.0f to designSystem.color(ColorToken.CHART_RMS).copy(alpha = (rmsLevel * 0.65f).coerceIn(0.08f, 0.7f)),
                    1.0f to Color.Transparent,
                    center = pRms,
                    radius = radius * 1.4f
                )
            )
            
            // Triangle Outline
            drawPath(
                path = trianglePath,
                color = designSystem.color(ColorToken.SURFACE_VARIANT).copy(alpha = 0.4f),
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Pure Minimalist Vertex Dots placed directly on corners (no text labels)
            drawCircle(
                color = designSystem.color(ColorToken.CHART_PITCH),
                radius = 5.dp.toPx(),
                center = pPitch
            )
            drawCircle(
                color = designSystem.color(ColorToken.CHART_RMS),
                radius = 5.dp.toPx(),
                center = pRms
            )
            drawCircle(
                color = designSystem.color(ColorToken.CHART_JITTER),
                radius = 5.dp.toPx(),
                center = pJitter
            )

            // Floating Barycentric Dot
            val weightJitter = jitterLevel.coerceIn(0.05f, 1f)
            val weightPitch = pitchLevel.coerceIn(0.05f, 1f)
            val weightRms = rmsLevel.coerceIn(0.05f, 1f)
            
            val total = weightJitter + weightPitch + weightRms
            val dotX = (weightJitter * pJitter.x + weightPitch * pPitch.x + weightRms * pRms.x) / total
            val dotY = (weightJitter * pJitter.y + weightPitch * pPitch.y + weightRms * pRms.y) / total
            
            // Glow around dot
            drawCircle(
                color = designSystem.color(ColorToken.TEXT_PRIMARY).copy(alpha = 0.15f),
                radius = 16.dp.toPx(),
                center = Offset(dotX, dotY)
            )
            drawCircle(
                color = designSystem.color(ColorToken.TEXT_PRIMARY),
                radius = 5.dp.toPx(),
                center = Offset(dotX, dotY)
            )
            drawCircle(
                color = designSystem.color(ColorToken.SURFACE_BACKGROUND),
                radius = 2.dp.toPx(),
                center = Offset(dotX, dotY)
            )
        }
    }
}
