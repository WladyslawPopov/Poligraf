package application.poligraf.ui.features.recorder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.DimenToken
import application.poligraf.ui.theme.tokens.IconToken

@Composable
fun AnalyzerControls(
    isRecording: Boolean,
    isPaused: Boolean,
    onStart: () -> Unit,
    onPauseResume: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val designSystem = LocalDesignSystem.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Center: Play/Pause
        FloatingActionButton(
            onClick = { if (isRecording) onPauseResume() else onStart() },
            shape = CircleShape,
            containerColor = if (isRecording && !isPaused) designSystem.color(ColorToken.STATE_SUCCESS) else designSystem.color(
                ColorToken.TEXT_PRIMARY
            ),
            contentColor = designSystem.color(ColorToken.TEXT_INVERTED),
            modifier = Modifier.size(designSystem.dimen(DimenToken.BUTTON_HEIGHT) + 16.dp)
        ) {
            val icon = when {
                !isRecording -> designSystem.icon(IconToken.MIC)
                isPaused -> designSystem.icon(IconToken.PLAY)
                else -> designSystem.icon(IconToken.PAUSE)
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
