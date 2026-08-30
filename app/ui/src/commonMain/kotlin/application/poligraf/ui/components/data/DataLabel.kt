package application.poligraf.ui.components.data

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken

@Composable
fun DataLabel(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: ColorToken = ColorToken.TEXT_PRIMARY,
    isVertical: Boolean = true
) {
    val designSystem = LocalDesignSystem.current
    
    if (isVertical) {
        Column(modifier = modifier) {
            Text(
                text = label,
                color = designSystem.color(ColorToken.TEXT_SECONDARY),
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = value,
                color = designSystem.color(valueColor),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        // Horizontal implementation can be added if needed
    }
}
