package application.poligraf.ui.components.decorators

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import application.poligraf.ui.theme.DesignSystem
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.DimenToken


@Composable
fun GlassDivider(designSystem: DesignSystem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(designSystem.dimen(DimenToken.DIVIDER_THICKNESS))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        designSystem.color(ColorToken.ACCENT_PRIMARY).copy(alpha = 0.3f),
                        Color.Transparent
                    )
                )
            )
    )
}
