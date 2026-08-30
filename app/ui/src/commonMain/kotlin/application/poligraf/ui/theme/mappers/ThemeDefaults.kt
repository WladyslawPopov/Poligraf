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

                ColorToken.CHART_JITTER -> Color(0xFF00FFCC)
                ColorToken.CHART_JITTER_ALARM -> Color(0xFFFF4444)
                ColorToken.CHART_PITCH -> Color(0xFF3399FF)
                ColorToken.CHART_PITCH_ALARM -> Color(0xFFFFEE00)
                ColorToken.CHART_RMS -> Color(0xFFFF9900)
                ColorToken.CHART_RMS_ALARM -> Color(0xFFAA00FF)
                ColorToken.CHART_ANOMALY -> Color(0xFFFF3B30)
            }
        } else {
            when (token) {
                ColorToken.SURFACE_BACKGROUND -> Color(0xFFE2E8F0) // Cleaner, lighter Slate grey
                ColorToken.SURFACE_PRIMARY -> Color(0xFFFFFFFF)    // Pure white for cards
                ColorToken.SURFACE_SECONDARY -> Color(0xFFF3F4F6)  // Very light grey for internal elements
                ColorToken.SURFACE_VARIANT -> Color(0xFF9CA3AF)    // Slightly darker borders
                ColorToken.GLASS_BASE -> Color(0x43fffdff)
                ColorToken.GLASS_BORDER -> Color(0x1A000000)
                ColorToken.ACCENT_PRIMARY -> Color(0xFF0E7490) // Deeper teal
                ColorToken.ACCENT_SECONDARY -> Color(0xFF155E75)
                ColorToken.ACCENT_ENERGY -> Color(0xFF06B6D4)
                ColorToken.STATE_SUCCESS -> Color(0xFF15803D) // More muted forest green
                ColorToken.STATE_ERROR -> Color(0xFFB91C1C)   // Softened red
                ColorToken.STATE_WARNING -> Color(0xFFB45309) // Deep amber instead of bright yellow
                ColorToken.STATE_INFO -> Color(0xFF1D4ED8)
                ColorToken.TEXT_PRIMARY -> Color(0xFF1E293B) // Slate blue-grey instead of near-black
                ColorToken.TEXT_SECONDARY -> Color(0xFF64748B)
                ColorToken.TEXT_INVERTED -> Color(0xffdddddd)

                ColorToken.RECORDER_PRIMARY -> Color(0xFFB91C1C)
                ColorToken.RECORDER_SECONDARY -> Color(0xFF1D4ED8)
                ColorToken.RECORDER_ACCENT -> Color(0xFFB45309)
                ColorToken.RECORDER_SURFACE -> Color(0xFFE2E8F0)
                ColorToken.RECORDER_WAVEFORM -> Color(0xFFB91C1C)

                ColorToken.CHART_JITTER -> Color(0xFF0F766E)
                ColorToken.CHART_JITTER_ALARM -> Color(0xFF991B1B)
                ColorToken.CHART_PITCH -> Color(0xFF0369A1)
                ColorToken.CHART_PITCH_ALARM -> Color(0xFF92400E)
                ColorToken.CHART_RMS -> Color(0xFFB45309)
                ColorToken.CHART_RMS_ALARM -> Color(0xFF6D28D9)
                ColorToken.CHART_ANOMALY -> Color(0xFFB91C1C)
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
