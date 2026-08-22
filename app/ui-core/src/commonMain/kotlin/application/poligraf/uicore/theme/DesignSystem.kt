package application.poligraf.uicore.theme

import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import application.poligraf.uicore.theme.mappers.IconMapper
import application.poligraf.uicore.theme.mappers.ThemeDefaults
import application.poligraf.uicore.theme.tokens.*

/**
 * Main entry point for the Design System logic.
 */
@Stable
class DesignSystem(
    val strings: AppUIStrings,
    val isDark: Boolean = true,
    val isDebug: Boolean = false
) {
    // Helper methods to get values easily in UI
    fun color(token: ColorToken): Color = ThemeDefaults.getColor(token, isDark)
    fun dimen(token: DimenToken): Dp = ThemeDefaults.getDimension(token)
    fun icon(token: IconToken): ImageVector = IconMapper.getIcon(token)
}

/**
 * CompositionLocal for Compose-based platforms.
 */
val LocalDesignSystem = staticCompositionLocalOf<DesignSystem> {
    error("DesignSystem not provided")
}
