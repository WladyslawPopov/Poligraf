package application.poligraf.ui.features.analyzer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.StringToken

/**
 * Compact color legend used under skins that do not label their metrics inline.
 */
@Composable
fun MetricLegend(modifier: Modifier = Modifier) {
    val designSystem = LocalDesignSystem.current

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(
            color = designSystem.color(ColorToken.CHART_PITCH),
            label = designSystem.string(StringToken.LABEL_STRESS)
        )
        LegendItem(
            color = designSystem.color(ColorToken.CHART_RMS),
            label = designSystem.string(StringToken.LABEL_PRESSURE)
        )
        LegendItem(
            color = designSystem.color(ColorToken.CHART_JITTER),
            label = designSystem.string(StringToken.LABEL_FEAR)
        )
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    val designSystem = LocalDesignSystem.current

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            color = designSystem.color(ColorToken.TEXT_SECONDARY),
            style = MaterialTheme.typography.labelSmall
        )
    }
}
