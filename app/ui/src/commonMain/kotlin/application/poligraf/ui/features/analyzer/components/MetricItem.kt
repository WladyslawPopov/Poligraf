package application.poligraf.ui.features.analyzer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken

@Composable
fun MetricItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val designSystem = LocalDesignSystem.current
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = color, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = label, color = designSystem.color(ColorToken.TEXT_SECONDARY), style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(8.dp))
        // Matching the colored indicator from mockup
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(3.dp)
                .background(color, CircleShape)
        )
    }
}
