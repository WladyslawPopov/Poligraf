package application.poligraf.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import application.poligraf.uicore.actions.WidgetAction
import application.poligraf.uicore.theme.DesignSystem
import application.poligraf.uicore.theme.LocalDesignSystem
import application.poligraf.uicore.theme.tokens.ColorToken
import application.poligraf.uicore.theme.tokens.IconToken
import application.poligraf.uicore.theme.tokens.DimenToken
import application.poligraf.uicore.widgets.UiWidget
import application.poligraf.widgets.utils.composeColor
import application.poligraf.widgets.utils.AppIcon

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SubjectListRenderer(
    widget: UiWidget.SubjectList,
    onAction: (WidgetAction) -> Unit
) {
    val designSystem = LocalDesignSystem.current

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = designSystem.dimen(DimenToken.SPACING_SMALL).dp)) {
        // Header moved OUTSIDE the container
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = designSystem.dimen(DimenToken.SPACING_LARGE).dp, 
                    vertical = designSystem.dimen(DimenToken.SPACING_SMALL).dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(
                icon = designSystem.icon(IconToken.HISTORY),
                contentDescription = null,
                tint = designSystem.composeColor(ColorToken.TEXT_SECONDARY),
                modifier = Modifier.size(designSystem.dimen(DimenToken.ICON_SIZE_TINY).dp)
            )
            Spacer(modifier = Modifier.width(designSystem.dimen(DimenToken.SPACING_SMALL).dp))
            Text(
                text = designSystem.strings.subjects.sectionRecordings.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = designSystem.composeColor(ColorToken.TEXT_SECONDARY),
                fontWeight = FontWeight.Bold
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = designSystem.dimen(DimenToken.SPACING_MEDIUM).dp),
            color = designSystem.composeColor(ColorToken.GLASS_BASE).copy(alpha = 0.3f),
            shape = MaterialTheme.shapes.large,
            border = BorderStroke(
                designSystem.dimen(DimenToken.DIVIDER_THICKNESS).dp,
                designSystem.composeColor(ColorToken.GLASS_BORDER).copy(alpha = 0.1f)
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Selection Panel
                AnimatedVisibility(
                    visible = widget.isSelectionMode,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    SelectionPanel(widget.selectedIds.size, onAction, designSystem)
                }

                // List Items
                Column(
                    modifier = Modifier.padding(vertical = designSystem.dimen(DimenToken.SPACING_TINY).dp)
                ) {
                    widget.items.forEachIndexed { index, item ->
                        val isSelected = widget.selectedIds.contains(item.id)
                        SubjectRowRenderer(item, isSelected, designSystem, onAction)
                        
                        if (index < widget.items.size - 1) {
                            // High-end Glassy Divider - ultra thin and subtle
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(designSystem.dimen(DimenToken.DIVIDER_THICKNESS).dp)
                                    .padding(horizontal = designSystem.dimen(DimenToken.SPACING_LARGE).dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                designSystem.composeColor(ColorToken.GLASS_BORDER).copy(alpha = 0.5f),
                                                designSystem.composeColor(ColorToken.GLASS_BORDER).copy(alpha = 0.15f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectionPanel(
    selectedCount: Int,
    onAction: (WidgetAction) -> Unit,
    designSystem: DesignSystem
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(designSystem.dimen(DimenToken.HEADER_HEIGHT).dp)
            .background(designSystem.composeColor(ColorToken.ACCENT_PRIMARY).copy(alpha = 0.05f))
            .padding(horizontal = designSystem.dimen(DimenToken.SPACING_MEDIUM).dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onAction(WidgetAction.ClearSelection) }) {
                AppIcon(
                    icon = designSystem.icon(IconToken.CLOSE),
                    contentDescription = null,
                    tint = designSystem.composeColor(ColorToken.TEXT_PRIMARY)
                )
            }
            Text(
                text = "$selectedCount ${designSystem.strings.subjects.actionSelected}",
                style = MaterialTheme.typography.titleSmall,
                color = designSystem.composeColor(ColorToken.TEXT_PRIMARY)
            )
        }

        Row {
            IconButton(onClick = { onAction(WidgetAction.DeleteSelected) }) {
                AppIcon(
                    icon = designSystem.icon(IconToken.DELETE),
                    contentDescription = null,
                    tint = designSystem.composeColor(ColorToken.ACCENT_PRIMARY)
                )
            }
            IconButton(onClick = { /* Future menu */ }) {
                AppIcon(
                    icon = designSystem.icon(IconToken.MORE_VERT),
                    contentDescription = null,
                    tint = designSystem.composeColor(ColorToken.TEXT_PRIMARY)
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
    val backgroundColor by animateColorAsState(
        if (isSelected) designSystem.composeColor(ColorToken.ACCENT_PRIMARY).copy(alpha = 0.12f)
        else Color.Transparent,
        label = "row_bg"
    )

    val indicatorWidth by animateDpAsState(
        if (isSelected) designSystem.dimen(DimenToken.SELECTION_INDICATOR_WIDTH).dp else 0.dp,
        label = "indicator_width"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(designSystem.dimen(DimenToken.SUBJECT_ROW_HEIGHT).dp)
            .background(backgroundColor)
            .combinedClickable(
                onClick = { onAction(item.action) },
                onLongClick = { onAction(WidgetAction.ToggleSelection(item.id)) }
            )
    ) {
        // Left Selection Indicator (Neon bar)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(indicatorWidth)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            designSystem.composeColor(ColorToken.ACCENT_PRIMARY).copy(alpha = 0.5f),
                            designSystem.composeColor(ColorToken.ACCENT_PRIMARY),
                            designSystem.composeColor(ColorToken.ACCENT_PRIMARY).copy(alpha = 0.5f)
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = designSystem.dimen(DimenToken.SPACING_MEDIUM).dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with animated border
            val avatarBg: Color = designSystem.composeColor(item.backgroundColor)
            val accentColor: Color = designSystem.composeColor(ColorToken.ACCENT_PRIMARY)
            
            Box(
                modifier = Modifier.size(designSystem.dimen(DimenToken.AVATAR_SIZE_SMALL).dp)
                    .clip(CircleShape)
                    .background(avatarBg.copy(alpha = 0.2f))
                    .then(
                        if (isSelected) Modifier.background(accentColor.copy(alpha = 0.2f)) 
                        else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = item.emoji, style = MaterialTheme.typography.headlineSmall)
                
                // Ring indicator
                if (isSelected) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Transparent,
                        shape = CircleShape,
                        border = BorderStroke(
                            designSystem.dimen(DimenToken.DIVIDER_THICKNESS).dp * 4, 
                            designSystem.composeColor(ColorToken.ACCENT_PRIMARY)
                        )
                    ) {}
                }
            }

            Spacer(modifier = Modifier.width(designSystem.dimen(DimenToken.SPACING_MEDIUM).dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    color = designSystem.composeColor(ColorToken.TEXT_PRIMARY),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                )
            }

            // Right Checkmark (clean and simple)
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(designSystem.composeColor(ColorToken.ACCENT_PRIMARY)),
                    contentAlignment = Alignment.Center
                ) {
                    AppIcon(
                        icon = designSystem.icon(IconToken.CHECK),
                        contentDescription = null,
                        tint = designSystem.composeColor(ColorToken.PRIMARY),
                        modifier = Modifier.size(designSystem.dimen(DimenToken.CHECKMARK_SIZE_SMALL).dp)
                    )
                }
            }
        }
    }
}
