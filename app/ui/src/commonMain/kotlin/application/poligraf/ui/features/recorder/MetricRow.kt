package application.poligraf.ui.features.recorder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.StringToken

@Composable
fun MetricRow(
    jitter: Float,
    pitch: Float,
    rms: Float,
    modifier: Modifier = Modifier
) {
    val designSystem = LocalDesignSystem.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        MetricItem(
            label = designSystem.string(StringToken.METRIC_JITTER),
            value = if (jitter > 0f) "${jitter.toInt()}%" else "0%",
            color = designSystem.color(ColorToken.CHART_JITTER)
        )
        MetricItem(
            label = designSystem.string(StringToken.METRIC_PITCH),
            value = if (pitch > 50f) pitch.toInt().toString() else "0",
            color = designSystem.color(ColorToken.CHART_PITCH)
        )
        MetricItem(
            label = designSystem.string(StringToken.METRIC_RMS),
            value = (rms * 100).toInt().coerceIn(0, 100).toString(),
            color = designSystem.color(ColorToken.CHART_RMS)
        )
    }
}
