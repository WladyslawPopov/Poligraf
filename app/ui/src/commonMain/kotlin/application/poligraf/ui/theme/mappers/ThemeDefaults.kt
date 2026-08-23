package application.poligraf.ui.theme.mappers

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.DimenToken

internal object ThemeDefaults {

    fun getColor(token: ColorToken, isDark: Boolean): Color {
        return if (isDark) {
            when (token) {
                ColorToken.SURFACE_BACKGROUND -> Color(0xFF121212)
                ColorToken.SURFACE_PRIMARY -> Color(0xFF1E1E1E)
                ColorToken.SURFACE_SECONDARY -> Color(0xFF2C2C2C)
                ColorToken.SURFACE_VARIANT -> Color(0xFF383838)
                ColorToken.GLASS_BASE -> Color(0x1AFFFFFF)
                ColorToken.GLASS_BORDER -> Color(0x33FFFFFF)
                ColorToken.ACCENT_PRIMARY -> Color(0xFF00F2FF)
                ColorToken.ACCENT_SECONDARY -> Color(0xFF00B8D4)
                ColorToken.ACCENT_ENERGY -> Color(0xFF00E5FF)
                ColorToken.STATE_SUCCESS -> Color(0xFF00E676)
                ColorToken.STATE_ERROR -> Color(0xFFFF5252)
                ColorToken.STATE_WARNING -> Color(0xFFFFD600)
                ColorToken.STATE_INFO -> Color(0xFF2196F3)
                ColorToken.TEXT_PRIMARY -> Color(0xFFFFFFFF)
                ColorToken.TEXT_SECONDARY -> Color(0xFFA0A0A0)
                ColorToken.TEXT_INVERTED -> Color(0xFF000000)

                ColorToken.RECORDER_PRIMARY -> Color(0xFFFF3B30)
                ColorToken.RECORDER_SECONDARY -> Color(0xFF007AFF)
                ColorToken.RECORDER_ACCENT -> Color(0xFFFFD600)
                ColorToken.RECORDER_SURFACE -> Color(0xFF1C1C1E)
                ColorToken.RECORDER_WAVEFORM -> Color(0xFFFF3B30)
            }
        } else {
            when (token) {
                ColorToken.SURFACE_BACKGROUND -> Color(0xFFF8FAFC)
                ColorToken.SURFACE_PRIMARY -> Color(0xFFFFFFFF)
                ColorToken.SURFACE_SECONDARY -> Color(0xFFF1F5F9)
                ColorToken.SURFACE_VARIANT -> Color(0xFFE2E8F0)
                ColorToken.GLASS_BASE -> Color(0xB3FFFFFF)
                ColorToken.GLASS_BORDER -> Color(0x33000000)
                ColorToken.ACCENT_PRIMARY -> Color(0xFF0891B2)
                ColorToken.ACCENT_SECONDARY -> Color(0xFF0E7490)
                ColorToken.ACCENT_ENERGY -> Color(0xFF22D3EE)
                ColorToken.STATE_SUCCESS -> Color(0xFF16A34A)
                ColorToken.STATE_ERROR -> Color(0xFFDC2626)
                ColorToken.STATE_WARNING -> Color(0xFFF59E0B)
                ColorToken.STATE_INFO -> Color(0xFF3B82F6)
                ColorToken.TEXT_PRIMARY -> Color(0xFF0F172A)
                ColorToken.TEXT_SECONDARY -> Color(0xFF64748B)
                ColorToken.TEXT_INVERTED -> Color(0xFFFFFFFF)

                ColorToken.RECORDER_PRIMARY -> Color(0xFFD32F2F)
                ColorToken.RECORDER_SECONDARY -> Color(0xFF1976D2)
                ColorToken.RECORDER_ACCENT -> Color(0xFFFFC107)
                ColorToken.RECORDER_SURFACE -> Color(0xFFF5F5F5)
                ColorToken.RECORDER_WAVEFORM -> Color(0xFFD32F2F)
            }
        }
    }

    fun getDimension(token: DimenToken): Dp {
        return when (token) {
            DimenToken.SPACING_TINY -> 4.dp
            DimenToken.SPACING_SMALL -> 8.dp
            DimenToken.SPACING_MEDIUM -> 16.dp
            DimenToken.SPACING_LARGE -> 24.dp
            DimenToken.SPACING_XL -> 32.dp
            DimenToken.SPACING_XXL -> 48.dp

            DimenToken.BUTTON_HEIGHT -> 56.dp
            DimenToken.HEADER_HEIGHT -> 64.dp
            DimenToken.DIVIDER_THICKNESS -> 0.5.dp
            
            DimenToken.MAX_CONTENT_WIDTH -> 600.dp
            DimenToken.RECORDER_BTN_SIZE -> 160.dp
            DimenToken.RECORDER_BTN_STROKE -> 4.dp
            DimenToken.BACKGROUND_CELL_SIZE -> 22.dp
        }
    }
}
