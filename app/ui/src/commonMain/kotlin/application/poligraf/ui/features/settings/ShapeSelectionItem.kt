package application.poligraf.ui.features.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import application.poligraf.domain.model.MarkerShape
import application.poligraf.ui.components.icons.AppIcon
import application.poligraf.ui.components.items.SelectableItem
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

    SelectableItem(
        isSelected = isSelected,
        onClick = onClick,
        modifier = modifier
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
