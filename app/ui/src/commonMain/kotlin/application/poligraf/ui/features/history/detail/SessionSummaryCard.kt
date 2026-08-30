package application.poligraf.ui.features.history.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import application.poligraf.ui.components.containers.AppCard
import application.poligraf.ui.components.data.DataLabel
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.DimenToken
import application.poligraf.ui.theme.tokens.StringToken

@Composable
fun SessionSummaryCard(
    volatilityStatus: StringToken,
    volatilityColor: ColorToken,
    anomalyCount: Int,
    durationText: String,
    conclusionText: StringToken,
    conclusionColor: ColorToken,
    modifier: Modifier = Modifier
) {
    val designSystem = LocalDesignSystem.current

    AppCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(designSystem.dimen(DimenToken.SPACING_MEDIUM))
    ) {
        Column {
            Text(
                text = designSystem.string(StringToken.HISTORY_SUMMARY_VOLATILITY),
                color = designSystem.color(ColorToken.TEXT_SECONDARY),
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = designSystem.string(volatilityStatus),
                color = designSystem.color(volatilityColor),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(designSystem.dimen(DimenToken.SPACING_MEDIUM)))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DataLabel(
                    label = designSystem.string(StringToken.HISTORY_SUMMARY_MARKERS),
                    value = anomalyCount.toString()
                )
                DataLabel(
                    label = designSystem.string(StringToken.HISTORY_SUMMARY_DURATION),
                    value = durationText
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = designSystem.dimen(DimenToken.SPACING_MEDIUM)),
                color = designSystem.color(ColorToken.SURFACE_VARIANT).copy(alpha = 0.5f)
            )

            Text(
                text = designSystem.string(conclusionText),
                color = designSystem.color(conclusionColor),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}
