package application.poligraf.ui.features.recorder

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
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
        
        val values = listOf(jitterLevel, pitchLevel, rmsLevel)
        val colors = listOf(
            designSystem.color(ColorToken.CHART_JITTER),
            designSystem.color(ColorToken.CHART_PITCH),
            designSystem.color(ColorToken.CHART_RMS)
        )
        
        values.forEachIndexed { index, value ->
            val radius = maxRadius - (index * 30.dp.toPx())
            val sweep = (value * 360f).coerceIn(10f, 360f)
            
            drawArc(
                color = colors[index].copy(alpha = 0.2f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 12.dp.toPx())
            )
            
            drawArc(
                color = colors[index],
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}
