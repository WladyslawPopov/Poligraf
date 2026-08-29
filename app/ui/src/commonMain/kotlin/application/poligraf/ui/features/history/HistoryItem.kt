package application.poligraf.ui.features.history

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.DimenToken
import application.poligraf.ui.theme.tokens.StringToken

@Composable
fun HistoryItem(
    title: String,
    dateText: String,
    markerCount: Int,
    modifier: Modifier = Modifier
) {
    val designSystem = LocalDesignSystem.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(designSystem.dimen(DimenToken.SPACING_MEDIUM))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = designSystem.color(ColorToken.TEXT_PRIMARY),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = designSystem.stringArgs(StringToken.HISTORY_ITEM_MARKERS, markerCount),
                color = designSystem.color(ColorToken.STATE_SUCCESS),
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = dateText,
            color = designSystem.color(ColorToken.TEXT_SECONDARY),
            style = MaterialTheme.typography.bodySmall
        )
    }
}
