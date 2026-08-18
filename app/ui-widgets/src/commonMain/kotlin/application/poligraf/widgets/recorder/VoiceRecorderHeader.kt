package application.poligraf.widgets.recorder

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import application.poligraf.uicore.state.VoiceRecorderAction
import application.poligraf.uicore.state.VoiceRecorderUiState
import application.poligraf.uicore.theme.DesignSystem
import application.poligraf.uicore.theme.tokens.ColorToken
import application.poligraf.uicore.theme.tokens.DimenToken
import application.poligraf.uicore.theme.tokens.IconToken
import application.poligraf.widgets.utils.composeColor
import application.poligraf.widgets.utils.AppIcon

@Composable
fun VoiceRecorderHeader(
    state: VoiceRecorderUiState,
    onAction: (VoiceRecorderAction) -> Unit,
    designSystem: DesignSystem
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = designSystem.dimen(DimenToken.SPACING_MEDIUM).dp)
    ) {
        if (state.header.isTrimming) {
            Text(
                text = designSystem.strings.recorder.trimCancel,
                color = designSystem.composeColor(ColorToken.TEXT_PRIMARY),
                fontSize = designSystem.dimen(DimenToken.TEXT_SIZE_TITLE_SMALL).sp,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clickable { onAction(VoiceRecorderAction.CancelTrim) }
            )
            
            // Title in center
            Text(
                text = state.header.title,
                color = designSystem.composeColor(ColorToken.TEXT_PRIMARY),
                fontWeight = FontWeight.Bold,
                fontSize = designSystem.dimen(DimenToken.TEXT_SIZE_TITLE_SMALL).sp,
                modifier = Modifier.align(Alignment.Center)
            )
            
            // Empty Box on the right to keep title centered
            Box(modifier = Modifier.align(Alignment.CenterEnd).size(36.dp))
        } else {
            Row(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(designSystem.dimen(DimenToken.SPACING_SMALL).dp)
            ) {
                // DISCARD (Close) Button
                Box(
                    modifier = Modifier.size(designSystem.dimen(DimenToken.RECORDER_DRAG_HANDLE_WIDTH).dp)
                        .clip(CircleShape)
                        .background(designSystem.composeColor(ColorToken.TEXT_PRIMARY).copy(alpha = 0.1f))
                        .clickable { onAction(VoiceRecorderAction.DiscardActive) },
                    contentAlignment = Alignment.Center
                ) {
                    AppIcon(
                        icon = designSystem.icon(IconToken.CLOSE),
                        contentDescription = null,
                        tint = designSystem.composeColor(ColorToken.TEXT_PRIMARY).copy(alpha = 0.6f),
                        modifier = Modifier.size(designSystem.dimen(DimenToken.ICON_SIZE_SMALL).dp)
                    )
                }

                // MENU Button
                Box(
                    modifier = Modifier.size(designSystem.dimen(DimenToken.RECORDER_DRAG_HANDLE_WIDTH).dp)
                        .clip(CircleShape)
                        .background(designSystem.composeColor(state.header.accentColor).copy(alpha = 0.15f))
                        .clickable { menuExpanded = true },
                    contentAlignment = Alignment.Center
                ) {
                    AppIcon(
                        icon = designSystem.icon(IconToken.MORE_HORIZ),
                        contentDescription = null,
                        tint = designSystem.composeColor(state.header.accentColor),
                        modifier = Modifier.size(designSystem.dimen(DimenToken.ICON_SIZE_NAV).dp)
                    )

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier
                            .background(designSystem.composeColor(state.surfaceColor))
                            .border(
                                width = designSystem.dimen(DimenToken.DIVIDER_THICKNESS).dp,
                                color = designSystem.composeColor(ColorToken.TEXT_PRIMARY).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(designSystem.dimen(DimenToken.SPACING_SMALL).dp)
                            )
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = designSystem.strings.recorder.uploadFile,
                                    color = designSystem.composeColor(ColorToken.TEXT_PRIMARY)
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onAction(VoiceRecorderAction.UploadFromFile)
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = designSystem.strings.recorder.trimMode,
                                    color = designSystem.composeColor(ColorToken.TEXT_PRIMARY)
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onAction(VoiceRecorderAction.ToggleTrimMode)
                            }
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(
                    text = state.header.title,
                    color = designSystem.composeColor(ColorToken.TEXT_PRIMARY),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    letterSpacing = (-0.2).sp
                )
                
                Text(
                    text = state.header.subtitle,
                    color = designSystem.composeColor(ColorToken.TEXT_PRIMARY).copy(alpha = 0.5f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(designSystem.composeColor(state.header.accentColor).copy(alpha = 0.15f))
                    .clickable { onAction(VoiceRecorderAction.SaveRecording) },
                contentAlignment = Alignment.Center
            ) {
                AppIcon(
                    icon = designSystem.icon(IconToken.CHECK),
                    contentDescription = null,
                    tint = designSystem.composeColor(state.header.accentColor),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
