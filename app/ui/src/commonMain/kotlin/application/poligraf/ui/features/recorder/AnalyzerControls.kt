package application.poligraf.ui.features.recorder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import application.poligraf.ui.foundation.types.AnalyzerSkin
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.DimenToken
import application.poligraf.ui.theme.tokens.IconToken

@Composable
fun AnalyzerControls(
    isRecording: Boolean,
    isPaused: Boolean,
    currentSkin: AnalyzerSkin,
    currentSkinName: String,
    onStart: () -> Unit,
    onPauseResume: () -> Unit,
    onSkinChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    val designSystem = LocalDesignSystem.current
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: Skin Switcher
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            Column(
                modifier = Modifier.padding(start = designSystem.dimen(DimenToken.SPACING_LARGE)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val skinIcon = when(currentSkin) {
                    AnalyzerSkin.STATE_MAP -> designSystem.icon(IconToken.SKIN_TRIANGLE) 
                    AnalyzerSkin.VOICE_RIBBON -> designSystem.icon(IconToken.SKIN_WAVE) 
                    AnalyzerSkin.EQUALIZER -> designSystem.icon(IconToken.SKIN_BARS) 
                    AnalyzerSkin.RINGS -> designSystem.icon(IconToken.SKIN_RINGS) 
                }
                
                IconButton(
                    onClick = onSkinChange,
                    modifier = Modifier.background(designSystem.color(ColorToken.SURFACE_VARIANT), CircleShape)
                ) {
                    Icon(
                        imageVector = skinIcon, 
                        contentDescription = null, 
                        tint = designSystem.color(ColorToken.TEXT_PRIMARY)
                    )
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = currentSkinName,
                    color = designSystem.color(ColorToken.TEXT_SECONDARY).copy(alpha = 0.6f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        // Center: Play/Pause
        FloatingActionButton(
            onClick = { if (isRecording) onPauseResume() else onStart() },
            shape = CircleShape,
            containerColor = if (isRecording && !isPaused) designSystem.color(ColorToken.STATE_SUCCESS) else designSystem.color(ColorToken.TEXT_PRIMARY),
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

        // Right side: Spacer for symmetry
        Spacer(modifier = Modifier.weight(1f))
    }
}
