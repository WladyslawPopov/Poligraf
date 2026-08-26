package application.poligraf.ui.features.recorder

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.StringToken

@Composable
fun StateMapVisualization(
    jitterLevel: Float, 
    pitchLevel: Float, 
    rmsLevel: Float,
    modifier: Modifier = Modifier
) {
    val designSystem = LocalDesignSystem.current
    val textMeasurer = rememberTextMeasurer()

    val stressText = designSystem.string(StringToken.LABEL_STRESS)
    val pressureText = designSystem.string(StringToken.LABEL_PRESSURE)
    val fearText = designSystem.string(StringToken.LABEL_FEAR)

    val stressStyle = MaterialTheme.typography.labelSmall.copy(
        fontSize = 12.sp,
        color = designSystem.color(ColorToken.CHART_PITCH)
    )
    val pressureStyle = MaterialTheme.typography.labelSmall.copy(
        fontSize = 12.sp,
        color = designSystem.color(ColorToken.CHART_RMS)
    )
    val fearStyle = MaterialTheme.typography.labelSmall.copy(
        fontSize = 12.sp,
        color = designSystem.color(ColorToken.CHART_JITTER)
    )

    Box(modifier = modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(280.dp)) {
            val center = Offset(size.width / 2, size.height / 2 + 6.dp.toPx())
            val radius = (size.minDimension / 2f) - 32.dp.toPx()
            
            // Vertices exactly aligned with triangle corners
            // Top: Stress (Pitch)
            val pPitch = Offset(center.x, center.y - radius) 
            // Bottom-Left: Pressure (RMS)
            val pRms = Offset(center.x - radius * 0.866f, center.y + radius * 0.5f) 
            // Bottom-Right: Fear (Jitter)
            val pJitter = Offset(center.x + radius * 0.866f, center.y + radius * 0.5f) 
            
            val trianglePath = Path().apply {
                moveTo(pPitch.x, pPitch.y)
                lineTo(pRms.x, pRms.y)
                lineTo(pJitter.x, pJitter.y)
                close()
            }
            
            // Dynamic Gradient Mesh based on vertex colors (Stress = Blue, Pressure = Orange, Fear = Green)
            drawPath(
                path = trianglePath,
                brush = Brush.radialGradient(
                    0.0f to designSystem.color(ColorToken.CHART_JITTER).copy(alpha = (jitterLevel * 0.45f).coerceIn(0.12f, 0.5f)),
                    1.0f to Color.Transparent,
                    center = pJitter,
                    radius = radius * 1.3f
                )
            )
            drawPath(
                path = trianglePath,
                brush = Brush.radialGradient(
                    0.0f to designSystem.color(ColorToken.CHART_PITCH).copy(alpha = (pitchLevel * 0.45f).coerceIn(0.12f, 0.5f)),
                    1.0f to Color.Transparent,
                    center = pPitch,
                    radius = radius * 1.3f
                )
            )
            drawPath(
                path = trianglePath,
                brush = Brush.radialGradient(
                    0.0f to designSystem.color(ColorToken.CHART_RMS).copy(alpha = (rmsLevel * 0.45f).coerceIn(0.12f, 0.5f)),
                    1.0f to Color.Transparent,
                    center = pRms,
                    radius = radius * 1.3f
                )
            )
            
            // Triangle Outline
            drawPath(
                path = trianglePath,
                color = designSystem.color(ColorToken.SURFACE_VARIANT).copy(alpha = 0.4f),
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Vertex Dots placed directly on corners
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

            // Draw text directly relative to vertex coordinates
            val stressLayout = textMeasurer.measure(stressText, stressStyle)
            drawText(
                textLayoutResult = stressLayout,
                topLeft = Offset(pPitch.x - stressLayout.size.width / 2, pPitch.y - stressLayout.size.height - 8.dp.toPx())
            )

            val pressureLayout = textMeasurer.measure(pressureText, pressureStyle)
            drawText(
                textLayoutResult = pressureLayout,
                topLeft = Offset(pRms.x - pressureLayout.size.width / 2, pRms.y + 10.dp.toPx())
            )

            val fearLayout = textMeasurer.measure(fearText, fearStyle)
            drawText(
                textLayoutResult = fearLayout,
                topLeft = Offset(pJitter.x - fearLayout.size.width / 2, pJitter.y + 10.dp.toPx())
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
