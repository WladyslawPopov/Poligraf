package application.liedetector.ui.components.widgets

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import application.liedetector.uicore.theme.tokens.ColorToken
import application.liedetector.uicore.theme.tokens.DimenToken
import application.liedetector.uicore.theme.LocalDesignSystem
import application.liedetector.uicore.theme.tokens.StringToken
import application.liedetector.uicore.theme.tokens.IconToken
import application.liedetector.uicore.widgets.UiWidget
import application.liedetector.theme.utils.composeColor
import application.liedetector.uicore.theme.DesignSystem
import application.liedetector.uicore.actions.WidgetAction

@Composable
fun SubjectListRenderer(
    widget: UiWidget.SubjectList,
    onAction: (WidgetAction) -> Unit
) {
    val designSystem = LocalDesignSystem.current
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = designSystem.dimen(DimenToken.SPACING_MEDIUM).dp)
    ) {
        Text(
            text = designSystem.string(StringToken.SECTION_SUBJECTS),
            style = MaterialTheme.typography.labelMedium,
            color = designSystem.composeColor(ColorToken.TEXT_SECONDARY),
            modifier = Modifier.padding(horizontal = designSystem.dimen(DimenToken.SPACING_MEDIUM).dp)
        )
        
        Spacer(modifier = Modifier.height(designSystem.dimen(DimenToken.SPACING_SMALL).dp))
        
        widget.items.forEachIndexed { index, item ->
            val isSelected = widget.selectedIds.contains(item.id)
            SubjectRowRenderer(item, isSelected, designSystem, onAction)
            
            if (index < widget.items.size - 1) {
                HorizontalDivider(
                    modifier = Modifier.padding(
                        start = designSystem.dimen(DimenToken.AVATAR_SIZE_SMALL).dp + 32.dp,
                        end = designSystem.dimen(DimenToken.SPACING_MEDIUM).dp
                    ),
                    thickness = 0.5.dp,
                    color = designSystem.composeColor(ColorToken.GLASS_BORDER).copy(alpha = 0.1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SubjectRowRenderer(
    item: UiWidget.SubjectCard,
    isSelected: Boolean,
    designSystem: DesignSystem,
    onAction: (WidgetAction) -> Unit
) {
    val backgroundColor = if (isSelected) {
        designSystem.composeColor(ColorToken.ACCENT_PRIMARY).copy(alpha = 0.1f)
    } else {
        Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(designSystem.dimen(DimenToken.SUBJECT_ROW_HEIGHT).dp)
            .background(backgroundColor)
            .combinedClickable(
                onClick = { onAction(item.action) },
                onLongClick = { onAction(WidgetAction.ToggleSelection(item.id)) }
            )
            .padding(horizontal = designSystem.dimen(DimenToken.SPACING_MEDIUM).dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(designSystem.dimen(DimenToken.AVATAR_SIZE_SMALL).dp)
                .background(
                    designSystem.composeColor(item.buttonColor).copy(alpha = 0.1f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = item.emoji, style = MaterialTheme.typography.titleLarge)
        }
        
        Spacer(modifier = Modifier.width(designSystem.dimen(DimenToken.SPACING_MEDIUM).dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title ?: designSystem.string(item.titleToken),
                style = MaterialTheme.typography.titleMedium,
                color = designSystem.composeColor(item.titleColor),
                fontWeight = FontWeight.SemiBold
            )
        }
        
        if (isSelected) {
            Icon(
                imageVector = designSystem.icon(IconToken.CHECK),
                contentDescription = null,
                tint = designSystem.composeColor(ColorToken.ACCENT_PRIMARY)
            )
        } else {
            Icon(
                imageVector = designSystem.icon(IconToken.CHEVRON_RIGHT),
                contentDescription = null,
                tint = designSystem.composeColor(ColorToken.TEXT_SECONDARY).copy(alpha = 0.5f)
            )
        }
    }
}
