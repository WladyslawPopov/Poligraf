package application.liedetector.theme.utils

import androidx.compose.ui.graphics.Color
import application.liedetector.uicore.theme.ColorToken
import application.liedetector.uicore.theme.DesignSystem
import androidx.core.graphics.toColorInt

/**
 * Converts Hex string to Compose Color.
 */
fun String.toComposeColor(): Color {
    return Color(this.toColorInt())
}

/**
 * Extension to get Compose Color directly from DesignSystem.
 */
fun DesignSystem.composeColor(token: ColorToken): Color {
    return this.color(token).toComposeColor()
}
