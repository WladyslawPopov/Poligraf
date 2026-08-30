package application.poligraf.ui.features.analyzer.parts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import application.poligraf.ui.components.status.StatusDot
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.StringToken

@Composable
fun AnalyzerHeader(
    durationText: String,
    isAnalyzing: Boolean,
    isPaused: Boolean,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val designSystem = LocalDesignSystem.current
    val isActionEnabled = isPaused || !isAnalyzing
    val disabledAlpha = 0.2f
    
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onDelete,
                enabled = isActionEnabled,
                border = BorderStroke(
                    1.dp,
                    if (isActionEnabled) designSystem.color(ColorToken.STATE_ERROR).copy(alpha = 0.3f) 
                    else designSystem.color(ColorToken.TEXT_SECONDARY).copy(alpha = disabledAlpha)
                ),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isActionEnabled) designSystem.color(ColorToken.STATE_ERROR) 
                                   else designSystem.color(ColorToken.TEXT_SECONDARY).copy(alpha = disabledAlpha)
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    designSystem.string(StringToken.DELETE), 
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusDot(
                        isAnalyzing = isAnalyzing && !isPaused,
                        size = 6.dp,
                        pulse = true
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = designSystem.string(StringToken.ACTIVE_SESSION),
                        color = designSystem.color(ColorToken.TEXT_SECONDARY),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Text(
                    text = durationText,
                    color = designSystem.color(ColorToken.TEXT_PRIMARY),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = onSave,
                enabled = isActionEnabled,
                border = BorderStroke(
                    1.dp,
                    if (isActionEnabled) designSystem.color(ColorToken.STATE_SUCCESS).copy(alpha = 0.3f)
                    else designSystem.color(ColorToken.TEXT_SECONDARY).copy(alpha = disabledAlpha)
                ),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isActionEnabled) designSystem.color(ColorToken.STATE_SUCCESS)
                                   else designSystem.color(ColorToken.TEXT_SECONDARY).copy(alpha = disabledAlpha)
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    designSystem.string(StringToken.SAVE), 
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
