package application.poligraf.ui.features.history.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import application.poligraf.ui.components.containers.AppCard
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.DimenToken
import application.poligraf.ui.theme.tokens.StringToken

@Composable
fun SessionSummaryCard(
    volatilityStatus: StringToken,
    volatilityColor: ColorToken,
    fullAnomalyCount: Int = 0,
    halftoneAnomalyCount: Int = 0,
    noteCount: Int = 0,
    durationText: String,
    durationMillis: Long = 0L,
    conclusionText: StringToken,
    conclusionColor: ColorToken,
    modifier: Modifier = Modifier
) {
    val designSystem = LocalDesignSystem.current

    val totalWindows = (durationMillis / 1000L).coerceAtLeast(1L).toFloat()
    val fullPercent = ((fullAnomalyCount / totalWindows) * 100f).let { if (it > 0f && it < 1f) "0.5" else it.toInt().toString() }
    val halftonePercent = ((halftoneAnomalyCount / totalWindows) * 100f).let { if (it > 0f && it < 1f) "0.5" else it.toInt().toString() }

    val fullText = if (fullAnomalyCount > 0) "$fullAnomalyCount ($fullPercent%)" else "0"
    val halftoneText = if (halftoneAnomalyCount > 0) "$halftoneAnomalyCount ($halftonePercent%)" else "0"

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

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🔴 Явные аномалии",
                        color = designSystem.color(ColorToken.TEXT_SECONDARY),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = fullText,
                        color = designSystem.color(ColorToken.TEXT_PRIMARY),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🟡 Полутоновые акценты",
                        color = designSystem.color(ColorToken.TEXT_SECONDARY),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = halftoneText,
                        color = designSystem.color(ColorToken.TEXT_PRIMARY),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "📝 Заметки",
                        color = designSystem.color(ColorToken.TEXT_SECONDARY),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = noteCount.toString(),
                        color = designSystem.color(ColorToken.TEXT_PRIMARY),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = designSystem.string(StringToken.HISTORY_SUMMARY_DURATION),
                        color = designSystem.color(ColorToken.TEXT_SECONDARY),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = durationText,
                        color = designSystem.color(ColorToken.TEXT_PRIMARY),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
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
