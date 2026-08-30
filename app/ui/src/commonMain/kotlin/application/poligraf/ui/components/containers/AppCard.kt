package application.poligraf.ui.components.containers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    backgroundColorToken: ColorToken = ColorToken.SURFACE_PRIMARY,
    borderColor: Color? = null,
    borderColorToken: ColorToken = ColorToken.SURFACE_VARIANT,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable () -> Unit
) {
    val designSystem = LocalDesignSystem.current
    
    val bg = backgroundColor ?: designSystem.color(backgroundColorToken)
    val border = borderColor ?: designSystem.color(borderColorToken).copy(alpha = 0.5f)
    
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(bg)
            .border(
                width = 1.dp,
                color = border,
                shape = MaterialTheme.shapes.medium
            )
            .padding(contentPadding)
    ) {
        content()
    }
}
