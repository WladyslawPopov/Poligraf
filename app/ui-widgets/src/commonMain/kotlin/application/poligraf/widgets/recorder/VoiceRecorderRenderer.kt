package application.poligraf.widgets.recorder

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
import application.poligraf.uicore.theme.tokens.DimenToken
import application.poligraf.widgets.VoiceRecorderWaveform
import application.poligraf.uicore.state.VoiceRecorderAction
import application.poligraf.uicore.state.VoiceRecorderUiState
import application.poligraf.uicore.theme.LocalDesignSystem
import application.poligraf.uicore.theme.tokens.ColorToken

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
        color = designSystem.color(state.surfaceColor),
        shape = RoundedCornerShape(
            topStart = designSystem.dimen(DimenToken.SPACING_XL),
            topEnd = designSystem.dimen(DimenToken.SPACING_XL)
        ),
        border = BorderStroke(
            designSystem.dimen(DimenToken.DIVIDER_THICKNESS),
            designSystem.color(ColorToken.TEXT_PRIMARY).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = designSystem.dimen(DimenToken.SPACING_XL)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag Handle (Simplified)
            Box(
                modifier = Modifier
                    .padding(vertical = designSystem.dimen(DimenToken.SPACING_SMALL))
                    .size(
                        width = designSystem.dimen(DimenToken.RECORDER_DRAG_HANDLE_WIDTH),
                        height = designSystem.dimen(DimenToken.RECORDER_DRAG_HANDLE_HEIGHT)
                    )
                    .clip(CircleShape)
                    .background(designSystem.color(ColorToken.TEXT_PRIMARY).copy(alpha = 0.3f))
            )

            VoiceRecorderHeader(state, onAction, designSystem)

            Spacer(modifier = Modifier.height(designSystem.dimen(DimenToken.SPACING_LARGE)))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(designSystem.dimen(DimenToken.RECORDER_WAVEFORM_HEIGHT))
            ) {
                VoiceRecorderWaveform(state, onAction, designSystem)
            }

            if (state.trim.isVisible) {
                Spacer(modifier = Modifier.height(designSystem.dimen(DimenToken.SPACING_XL)))
                MiniTrimOverview(state, onAction, designSystem)
            }

            Spacer(modifier = Modifier.weight(1f))

            // Total Duration Label
            Text(
                text = state.header.timerLabel,
                color = designSystem.color(ColorToken.TEXT_PRIMARY).copy(alpha = 0.4f),
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
                    fontSize = designSystem.dimen(DimenToken.TEXT_SIZE_TITLE_LARGE).value.sp,
                    letterSpacing = (-1).sp
                ),
                color = designSystem.color(ColorToken.TEXT_PRIMARY)
            )

            Spacer(modifier = Modifier.height(designSystem.dimen(DimenToken.SPACING_XL)))

            VoiceRecorderControls(state, onAction, designSystem)
        }
    }
}
