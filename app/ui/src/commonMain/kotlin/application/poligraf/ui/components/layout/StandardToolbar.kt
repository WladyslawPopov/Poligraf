package application.poligraf.ui.components.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import application.poligraf.ui.components.buttons.AppIconButton
import application.poligraf.ui.components.status.StatusDot
import application.poligraf.ui.components.text.TypingText
import application.poligraf.ui.foundation.models.AppToolbar
import application.poligraf.ui.theme.DesignSystem
import application.poligraf.ui.theme.tokens.ColorToken

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardToolbar(
    toolbar: AppToolbar,
    designSystem: DesignSystem,
    onNavigationClick: () -> Unit = {},
    onAction: (Any) -> Unit = {},
    isTyping: Boolean = false,
    durationText: String? = null,
    isAnalyzing: Boolean = false,
    isProcessing: Boolean = false,
    showIndicator: Boolean = true
) {
    CenterAlignedTopAppBar(
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isTyping) {
                    TypingText(
                        fullText = designSystem.string(toolbar.titleToken),
                        color = designSystem.color(toolbar.contentColor),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = designSystem.string(toolbar.titleToken),
                        color = designSystem.color(toolbar.contentColor),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                }

                if (durationText != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        if (showIndicator) {
                            StatusDot(
                                isAnalyzing = isAnalyzing,
                                size = 8.dp,
                                pulse = true
                            )
                        }
                        Text(
                            text = durationText,
                            style = MaterialTheme.typography.titleSmall,
                            color = designSystem.color(toolbar.contentColor).copy(alpha = 0.9f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        navigationIcon = {
            toolbar.navigationIcon?.let { icon ->
                AppIconButton(
                    icon = icon,
                    enabled = !isProcessing,
                    onClick = {
                        toolbar.navigationAction?.let(onAction) ?: onNavigationClick()
                    }
                )
            }
        },
        actions = {
            toolbar.trailingActions.forEach { actionItem ->
                val isActionEnabled = !isAnalyzing && !isProcessing // Disable delete/save actions during active analyzing or processing
                
                AppIconButton(
                    icon = actionItem.icon,
                    tint = actionItem.tint ?: toolbar.contentColor,
                    enabled = isActionEnabled,
                    onClick = { 
                        onAction(actionItem.action)
                    }
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent
        ),
        windowInsets = TopAppBarDefaults.windowInsets
    )
}
