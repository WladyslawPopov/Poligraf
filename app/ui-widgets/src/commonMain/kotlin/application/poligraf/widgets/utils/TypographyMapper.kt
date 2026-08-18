package application.poligraf.widgets.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import application.poligraf.uicore.theme.DesignSystem
import application.poligraf.uicore.theme.tokens.TypographyToken

/**
 * Maps TypographyToken to MaterialTheme typography.
 */
@Composable
fun DesignSystem.typography(token: TypographyToken): TextStyle {
    return when (token) {
        TypographyToken.HEADER -> MaterialTheme.typography.displaySmall
        TypographyToken.SUBHEADER -> MaterialTheme.typography.titleLarge
        TypographyToken.BODY -> MaterialTheme.typography.bodyMedium
        TypographyToken.CAPTION -> MaterialTheme.typography.labelSmall
        TypographyToken.DATA_NUMERIC -> MaterialTheme.typography.headlineMedium
    }
}
