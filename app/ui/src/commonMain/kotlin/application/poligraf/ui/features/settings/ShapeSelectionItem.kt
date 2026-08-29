package application.poligraf.ui.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import application.poligraf.domain.model.MarkerShape
import application.poligraf.ui.components.icons.AppIcon
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.IconToken

@Composable
fun ShapeSelectionItem(
    shape: MarkerShape,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val designSystem = LocalDesignSystem.current
    val icon = when (shape) {
        MarkerShape.CIRCLE -> IconToken.SHAPE_CIRCLE
        MarkerShape.STAR -> IconToken.SHAPE_STAR
        MarkerShape.DIAMOND -> IconToken.SHAPE_DIAMOND
        MarkerShape.HEART -> IconToken.SHAPE_HEART
    }

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(
                if (isSelected) designSystem.color(ColorToken.SURFACE_PRIMARY) else designSystem.color(
                    ColorToken.SURFACE_SECONDARY
                ).copy(alpha = 0.5f)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) designSystem.color(ColorToken.ACCENT_PRIMARY) else Color.Transparent,
                shape = MaterialTheme.shapes.medium
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        AppIcon(
            icon = designSystem.icon(icon),
            contentDescription = null,
            tint = if (isSelected) designSystem.color(ColorToken.TEXT_PRIMARY) else designSystem.color(
                ColorToken.TEXT_SECONDARY
            ).copy(alpha = 0.6f)
        )
    }
}
