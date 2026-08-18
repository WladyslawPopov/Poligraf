package application.liedetector.widgets.utils

import androidx.compose.ui.graphics.Color
import application.liedetector.uicore.theme.DesignSystem
import application.liedetector.uicore.theme.tokens.ColorToken

/**
 * Converts Hex string to Compose Color.
 * Supports #RRGGBB and #AARRGGBB formats.
 */
fun String.toComposeColor(): Color {
    val hex = this.removePrefix("#")
    return try {
        when (hex.length) {
            6 -> Color(0xFF000000 or hex.toLong(16))
            8 -> Color(hex.toLong(16))
            else -> Color.Transparent
        }
    } catch (e: Exception) {
        Color.Transparent
    }
}

/**
 * Extension to get Compose Color directly from DesignSystem.
 */
fun DesignSystem.composeColor(token: ColorToken): Color {
    return this.color(token).toComposeColor()
}
