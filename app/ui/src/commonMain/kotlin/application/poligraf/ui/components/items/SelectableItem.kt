package application.poligraf.ui.components.items

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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken

@Composable
fun SelectableItem(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp? = 48.dp,
    content: @Composable () -> Unit
) {
    val designSystem = LocalDesignSystem.current
    
    val baseModifier = modifier
        .clip(MaterialTheme.shapes.medium)
        .background(
            if (isSelected) designSystem.color(ColorToken.SURFACE_PRIMARY) 
            else designSystem.color(ColorToken.SURFACE_SECONDARY).copy(alpha = 0.5f)
        )
        .border(
            width = 1.dp,
            color = if (isSelected) designSystem.color(ColorToken.ACCENT_PRIMARY) else Color.Transparent,
            shape = MaterialTheme.shapes.medium
        )
        .clickable(onClick = onClick)

    val finalModifier = if (size != null) baseModifier.size(size) else baseModifier

    Box(
        modifier = finalModifier,
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
