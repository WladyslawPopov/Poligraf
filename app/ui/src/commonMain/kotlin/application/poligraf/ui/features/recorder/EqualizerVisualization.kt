package application.poligraf.ui.features.recorder

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.StringToken

@Composable
fun EqualizerVisualization(
    jitterLevel: Float,
    pitchLevel: Float,
    rmsLevel: Float,
    modifier: Modifier = Modifier
) {
    val designSystem = LocalDesignSystem.current
    Box(modifier = modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerY = size.height / 2
            val barWidth = 44.dp.toPx()
            val spacing = 64.dp.toPx()
            val centerX = size.width / 2
            
            val values = listOf(jitterLevel, pitchLevel, rmsLevel)
            val colorPairs = listOf(
                designSystem.color(ColorToken.CHART_JITTER) to designSystem.color(ColorToken.CHART_JITTER_ALARM),
                designSystem.color(ColorToken.CHART_PITCH) to designSystem.color(ColorToken.CHART_PITCH_ALARM),
                designSystem.color(ColorToken.CHART_RMS) to designSystem.color(ColorToken.CHART_RMS_ALARM)
            )
            
            values.forEachIndexed { index, value ->
                val x = centerX + (index - 1) * (barWidth + spacing)
                val barHeight = (value * size.height * 0.4f).coerceIn(4.dp.toPx(), size.height * 0.45f)
                
                // Studio-style VU gradient (Calm center color, intense alarm color at edges)
                drawRect(
                    brush = Brush.verticalGradient(
                        0.0f to colorPairs[index].second,
                        0.5f to colorPairs[index].first,
                        1.0f to colorPairs[index].second
                    ),
                    topLeft = Offset(x - barWidth/2, centerY - barHeight),
                    size = Size(barWidth, barHeight * 2)
                )
            }
            
            // Baseline Divider
            drawLine(
                color = designSystem.color(ColorToken.SURFACE_VARIANT).copy(alpha = 0.5f),
                start = Offset(centerX - (barWidth + spacing) * 1.5f, centerY),
                end = Offset(centerX + (barWidth + spacing) * 1.5f, centerY),
                strokeWidth = 1.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
        
        Text(
            text = designSystem.string(StringToken.LABEL_ZERO),
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 40.dp),
            color = designSystem.color(ColorToken.TEXT_SECONDARY).copy(alpha = 0.5f),
            style = MaterialTheme.typography.labelSmall
        )
    }
}
