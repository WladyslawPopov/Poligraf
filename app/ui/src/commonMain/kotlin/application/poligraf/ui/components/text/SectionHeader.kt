package application.poligraf.ui.components.text

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.StringToken

@Composable
fun SectionHeader(
    titleToken: StringToken,
    modifier: Modifier = Modifier,
    subtitleToken: StringToken? = null,
    isLarge: Boolean = false
) {
    val designSystem = LocalDesignSystem.current
    
    Column(modifier = modifier) {
        Text(
            text = designSystem.string(titleToken),
            style = if (isLarge) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
            color = designSystem.color(ColorToken.TEXT_PRIMARY),
            fontWeight = FontWeight.Bold
        )
        
        subtitleToken?.let {
            Spacer(Modifier.height(4.dp))
            Text(
                text = designSystem.string(it),
                style = MaterialTheme.typography.labelMedium,
                color = designSystem.color(ColorToken.TEXT_SECONDARY)
            )
        }
    }
}
