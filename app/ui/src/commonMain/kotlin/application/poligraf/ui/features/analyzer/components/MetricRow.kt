package application.poligraf.ui.features.analyzer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.StringToken
import kotlin.math.roundToInt

@Composable
fun MetricRow(
    jitterLevel: Float,
    pitchLevel: Float,
    rmsLevel: Float,
    modifier: Modifier = Modifier
) {
    val designSystem = LocalDesignSystem.current

    val safeJitter = if (jitterLevel.isNaN() || jitterLevel.isInfinite()) 0f else jitterLevel
    val safePitch = if (pitchLevel.isNaN() || pitchLevel.isInfinite()) 0f else pitchLevel
    val safeRms = if (rmsLevel.isNaN() || rmsLevel.isInfinite()) 0f else rmsLevel

    val jitterPercent = (safeJitter * 100f).roundToInt().coerceIn(0, 100)
    val pitchPercent = (safePitch * 100f).roundToInt().coerceIn(0, 100)
    val rmsPercent = (safeRms * 100f).roundToInt().coerceIn(0, 100)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        MetricItem(
            label = designSystem.string(StringToken.METRIC_JITTER),
            value = "$jitterPercent%",
            color = designSystem.color(ColorToken.CHART_JITTER)
        )
        MetricItem(
            label = designSystem.string(StringToken.METRIC_PITCH),
            value = "$pitchPercent%",
            color = designSystem.color(ColorToken.CHART_PITCH)
        )
        MetricItem(
            label = designSystem.string(StringToken.METRIC_RMS),
            value = "$rmsPercent%",
            color = designSystem.color(ColorToken.CHART_RMS)
        )
    }
}
