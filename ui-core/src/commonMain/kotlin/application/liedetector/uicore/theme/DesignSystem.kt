package application.liedetector.uicore.theme

import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Main entry point for the Design System logic.
 */
@Stable
class DesignSystem(
    val resources: ResourceProvider,
    val isDark: Boolean = true
) {
    // Helper methods to get values easily in UI
    fun color(token: ColorToken): String = resources.getColorHex(token, isDark)
    fun dimen(token: DimenToken): Float = resources.getDimension(token)
    fun string(token: StringToken): String = resources.getString(token)
    fun icon(token: IconToken): IconResource = resources.getIcon(token)
}

/**
 * CompositionLocal for Compose-based platforms.
 */
val LocalDesignSystem = staticCompositionLocalOf<DesignSystem> {
    error("DesignSystem not provided")
}
