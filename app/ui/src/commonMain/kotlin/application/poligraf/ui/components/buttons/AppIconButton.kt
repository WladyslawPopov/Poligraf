package application.poligraf.ui.components.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.DimenToken
import application.poligraf.ui.theme.tokens.IconToken
import application.poligraf.ui.components.icons.AppIcon

@Composable
fun AppIconButton(
    icon: IconToken,
    tint: ColorToken = ColorToken.TEXT_PRIMARY,
    contentDescription: String = "",
    onClick: () -> Unit
) {
    val designSystem = LocalDesignSystem.current

    IconButton(
        onClick = onClick,
        modifier = Modifier
            .padding(start = designSystem.dimen(DimenToken.SPACING_SMALL))
            .clip(CircleShape)
            .background(
                designSystem.color(ColorToken.GLASS_BASE)
                    .copy(alpha = 0.3f)
            )
    ) {
        AppIcon(
            icon = designSystem.icon(icon),
            contentDescription = contentDescription,
            tint = designSystem.color(tint)
        )
    }
}
