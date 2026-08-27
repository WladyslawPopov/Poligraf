package application.poligraf.ui.theme

import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import application.poligraf.ui.theme.mappers.IconMapper
import application.poligraf.ui.theme.mappers.StringMapper
import application.poligraf.ui.theme.mappers.ThemeDefaults
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.DimenToken
import application.poligraf.ui.theme.tokens.IconToken
import application.poligraf.ui.theme.tokens.StringToken

/**
 * Main entry point for the Design System logic.
 */
@Stable
class DesignSystem(
    val strings: AppStrings,
    val isDark: Boolean = true,
    val isDebug: Boolean = false
) {
    fun color(token: ColorToken): Color = ThemeDefaults.getColor(token, isDark)
    fun dimen(token: DimenToken): Dp = ThemeDefaults.getDimension(token)
    fun icon(token: IconToken): ImageVector = IconMapper.getIcon(token)
    fun string(token: StringToken): String = StringMapper.getString(token, strings)
    
    fun stringArgs(token: StringToken, vararg args: Any): String {
        var base = string(token)
        args.forEach { arg ->
            base = base.replaceFirst("%s", arg.toString()).replaceFirst("%d", arg.toString())
        }
        return base
    }
}

/**
 * CompositionLocal for Compose-based platforms.
 */
val LocalDesignSystem = staticCompositionLocalOf<DesignSystem> {
    error("DesignSystem not provided")
}
