package application.liedetector.widgets.recorder

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import application.liedetector.uicore.state.VoiceRecorderAction
import application.liedetector.uicore.state.VoiceRecorderUiState
import application.liedetector.uicore.theme.DesignSystem
import application.liedetector.uicore.theme.tokens.ColorToken
import application.liedetector.uicore.theme.tokens.DimenToken
import application.liedetector.uicore.theme.tokens.StringToken
import application.liedetector.widgets.utils.composeColor
import application.liedetector.widgets.utils.AppIcon

@Composable
fun VoiceRecorderControls(
    state: VoiceRecorderUiState,
    onAction: (VoiceRecorderAction) -> Unit,
    designSystem: DesignSystem
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onAction(VoiceRecorderAction.Skip(-15000)) }) {
            AppIcon(
                icon = designSystem.icon(state.controls.skipBackIcon),
                contentDescription = null,
                tint = designSystem.composeColor(ColorToken.TEXT_PRIMARY),
                modifier = Modifier.size(designSystem.dimen(DimenToken.SPACING_XL).dp + 4.dp)
            )
        }
        Spacer(modifier = Modifier.width(48.dp))
        IconButton(onClick = { onAction(VoiceRecorderAction.TogglePlay) }) {
            AppIcon(
                icon = designSystem.icon(state.controls.playbackIcon),
                contentDescription = null,
                tint = designSystem.composeColor(ColorToken.TEXT_PRIMARY),
                modifier = Modifier.size(56.dp)
            )
        }
        Spacer(modifier = Modifier.width(48.dp))
        IconButton(onClick = { onAction(VoiceRecorderAction.Skip(15000)) }) {
            AppIcon(
                icon = designSystem.icon(state.controls.skipForwardIcon),
                contentDescription = null,
                tint = designSystem.composeColor(ColorToken.TEXT_PRIMARY),
                modifier = Modifier.size(designSystem.dimen(DimenToken.SPACING_XL).dp + 4.dp).graphicsLayer(scaleX = -1f)
            )
        }
    }

    Spacer(modifier = Modifier.height(32.dp))

    // Bottom Action Bar (Record/Trim)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = designSystem.dimen(DimenToken.SPACING_LARGE).dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (state.trim.isVisible) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(designSystem.composeColor(ColorToken.TEXT_PRIMARY).copy(alpha = 0.1f))
                    .clickable { onAction(VoiceRecorderAction.ApplyTrim(state.trim.startMillis, state.trim.endMillis)) }
                    .padding(
                        horizontal = designSystem.dimen(DimenToken.SPACING_XL).dp * 1.5f,
                        vertical = designSystem.dimen(DimenToken.SPACING_MEDIUM).dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = designSystem.string(StringToken.RECORDER_TRIM),
                    color = designSystem.composeColor(ColorToken.TEXT_PRIMARY),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Spacer(modifier = Modifier.width(designSystem.dimen(DimenToken.AVATAR_SIZE_SMALL).dp))

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(designSystem.composeColor(state.controls.recordButtonColor))
                    .clickable { onAction(VoiceRecorderAction.ToggleRecord) },
                contentAlignment = Alignment.Center
            ) {
                AppIcon(
                    icon = designSystem.icon(state.controls.recordIcon),
                    contentDescription = null,
                    tint = designSystem.composeColor(ColorToken.TEXT_INVERTED),
                    modifier = Modifier.size(designSystem.dimen(DimenToken.SPACING_XL).dp)
                )
            }

            Spacer(modifier = Modifier.width(designSystem.dimen(DimenToken.AVATAR_SIZE_SMALL).dp))
        }
    }
}
