package application.poligraf.uicore.theme

import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.vector.ImageVector
import application.poligraf.uicore.theme.tokens.*

/**
 * Main entry point for the Design System logic.
 */
@Stable
class DesignSystem(
    val resources: ResourceProvider,
    val strings: AppUIStrings,
    val isDark: Boolean = true,
    val isDebug: Boolean = false
) {
    // Helper methods to get values easily in UI
    fun color(token: ColorToken): String = resources.getColorHex(token, isDark)
    fun dimen(token: DimenToken): Float = resources.getDimension(token)
    fun icon(token: IconToken): ImageVector = IconMapper.getIcon(token)
}

/**
 * CompositionLocal for Compose-based platforms.
 */
val LocalDesignSystem = staticCompositionLocalOf<DesignSystem> {
    error("DesignSystem not provided")
}
