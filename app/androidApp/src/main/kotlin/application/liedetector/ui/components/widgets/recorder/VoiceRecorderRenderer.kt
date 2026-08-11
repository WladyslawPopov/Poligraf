package application.liedetector.ui.components.widgets.recorder

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import application.liedetector.presentation.recordingHistory.VoiceRecorderAction
import application.liedetector.presentation.recordingHistory.VoiceRecorderUiState
import application.liedetector.theme.utils.composeColor
import application.liedetector.uicore.theme.LocalDesignSystem
import application.liedetector.uicore.theme.tokens.ColorToken
import application.liedetector.uicore.theme.tokens.DimenToken

@Composable
fun VoiceRecorderRenderer(
    state: VoiceRecorderUiState,
    onAction: (VoiceRecorderAction) -> Unit
) {
    val designSystem = LocalDesignSystem.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        color = designSystem.composeColor(state.surfaceColor),
        shape = RoundedCornerShape(
            topStart = designSystem.dimen(DimenToken.SPACING_XL).dp,
            topEnd = designSystem.dimen(DimenToken.SPACING_XL).dp
        ),
        border = BorderStroke(
            designSystem.dimen(DimenToken.DIVIDER_THICKNESS).dp,
            designSystem.composeColor(ColorToken.TEXT_PRIMARY).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = designSystem.dimen(DimenToken.SPACING_XL).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag Handle (Simplified)
            Box(
                modifier = Modifier
                    .padding(vertical = designSystem.dimen(DimenToken.SPACING_SMALL).dp)
                    .size(
                        width = designSystem.dimen(DimenToken.RECORDER_DRAG_HANDLE_WIDTH).dp,
                        height = designSystem.dimen(DimenToken.RECORDER_DRAG_HANDLE_HEIGHT).dp
                    )
                    .clip(CircleShape)
                    .background(designSystem.composeColor(ColorToken.TEXT_PRIMARY).copy(alpha = 0.3f))
            )

            VoiceRecorderHeader(state, onAction, designSystem)

            Spacer(modifier = Modifier.height(designSystem.dimen(DimenToken.SPACING_LARGE).dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(designSystem.dimen(DimenToken.RECORDER_WAVEFORM_HEIGHT).dp)
            ) {
                VoiceRecorderWaveform(state, onAction, designSystem)
            }

            if (state.trim.isVisible) {
                Spacer(modifier = Modifier.height(designSystem.dimen(DimenToken.SPACING_XL).dp))
                MiniTrimOverview(state, onAction, designSystem)
            }

            Spacer(modifier = Modifier.weight(1f))

            // Total Duration Label
            Text(
                text = state.header.timerLabel,
                color = designSystem.composeColor(ColorToken.TEXT_PRIMARY).copy(alpha = 0.4f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Timer
            Text(
                text = state.header.timerLabelPrecise,
                style = LocalTextStyle.current.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    fontSize = designSystem.dimen(DimenToken.TEXT_SIZE_TITLE_LARGE).sp,
                    letterSpacing = (-1).sp
                ),
                color = designSystem.composeColor(ColorToken.TEXT_PRIMARY)
            )

            Spacer(modifier = Modifier.height(designSystem.dimen(DimenToken.SPACING_XL).dp))

            VoiceRecorderControls(state, onAction, designSystem)
        }
    }
}
