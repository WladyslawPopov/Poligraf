package application.poligraf.ui.features.analyzer.visualizations

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken

@Composable
fun RingsVisualization(
    jitterLevel: Float,
    pitchLevel: Float,
    rmsLevel: Float,
    modifier: Modifier = Modifier
) {
    val designSystem = LocalDesignSystem.current

    Canvas(modifier = modifier.size(300.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val maxRadius = size.minDimension / 2.2f
        
        val values = listOf(pitchLevel, rmsLevel, jitterLevel)
        val colors = listOf(
            designSystem.color(ColorToken.CHART_PITCH),
            designSystem.color(ColorToken.CHART_RMS),
            designSystem.color(ColorToken.CHART_JITTER)
        )
        
        values.forEachIndexed { index, value ->
            val radius = maxRadius - (index * 30.dp.toPx())
            val sweep = (value * 360f).coerceIn(2f, 360f)
            
            // Background Track
            drawArc(
                color = colors[index].copy(alpha = 0.12f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 12.dp.toPx())
            )
            
            // Core Visible Arc
            drawArc(
                color = colors[index],
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            )

            // Dynamic Shadow Glow (Grows with intensity/value)
            // This creates a "light bleed" effect around the rings
            if (value > 0.05f) {
                drawArc(
                    color = colors[index].copy(alpha = (value * 0.5f).coerceAtMost(0.6f)),
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(
                        width = (12.dp.toPx() + (value * 32.dp.toPx())), 
                        cap = StrokeCap.Round
                    )
                )
            }
        }
    }
}
