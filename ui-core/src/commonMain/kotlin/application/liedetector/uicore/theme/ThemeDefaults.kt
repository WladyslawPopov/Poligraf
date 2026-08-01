package application.liedetector.uicore.theme

import application.liedetector.uicore.theme.tokens.*

/**
 * Central repository for all Design System values.
 * This ensures consistency across Android and iOS.
 */
object ThemeDefaults {

    fun getColorHex(token: ColorToken, isDark: Boolean): String {
        return if (isDark) {
            when (token) {
                ColorToken.BACKGROUND -> "#121212"
                ColorToken.SURFACE -> "#1E1E1E"
                ColorToken.SURFACE_VARIANT -> "#2C2C2C"
                ColorToken.GLASS_BASE -> "#1AFFFFFF"
                ColorToken.GLASS_BORDER -> "#33FFFFFF"
                ColorToken.PRIMARY -> "#D1D1D1"
                ColorToken.ON_PRIMARY -> "#000000"
                ColorToken.TRUTH -> "#00E676"
                ColorToken.STRESS -> "#FF5252"
                ColorToken.ACCENT_PRIMARY -> "#00F2FF"
                ColorToken.ACCENT_ENERGY -> "#00B8D4"
                ColorToken.ERROR -> "#CF6679"
                ColorToken.TEXT_PRIMARY -> "#FFFFFF"
                ColorToken.TEXT_SECONDARY -> "#A0A0A0"
                ColorToken.TEXT_INVERTED -> "#000000"
            }
        } else {
            when (token) {
                ColorToken.BACKGROUND -> "#CBD5E1"
                ColorToken.SURFACE -> "#FFFFFF"
                ColorToken.SURFACE_VARIANT -> "#94A3B8"
                ColorToken.GLASS_BASE -> "#B3FFFFFF"
                ColorToken.GLASS_BORDER -> "#33000000"
                ColorToken.PRIMARY -> "#334155"
                ColorToken.ON_PRIMARY -> "#FFFFFF"
                ColorToken.TRUTH -> "#16A34A"
                ColorToken.STRESS -> "#DC2626"
                ColorToken.ACCENT_PRIMARY -> "#0891B2"
                ColorToken.ACCENT_ENERGY -> "#22D3EE"
                ColorToken.ERROR -> "#B00020"
                ColorToken.TEXT_PRIMARY -> "#0F172A"
                ColorToken.TEXT_SECONDARY -> "#64748B"
                ColorToken.TEXT_INVERTED -> "#FFFFFF"
            }
        }
    }

    fun getDimension(token: DimenToken): Float {
        return when (token) {
            DimenToken.MAIN_PADDING -> 16f
            DimenToken.WIDGET_SPACING -> 12f
            DimenToken.CORNER_RADIUS -> 12f
            DimenToken.ICON_SIZE_NAV -> 24f
            DimenToken.ICON_SIZE_SMALL -> 18f
            DimenToken.ICON_SIZE_LARGE -> 38f
            DimenToken.HEADER_HEIGHT -> 64f
            DimenToken.DRAWER_CORNER -> 24f
            DimenToken.WIDGET_CORNER -> 18f
            DimenToken.BUTTON_HEIGHT -> 56f
            DimenToken.SPACING_TINY -> 4f
            DimenToken.SPACING_SMALL -> 8f
            DimenToken.SPACING_MEDIUM -> 16f
            DimenToken.SPACING_LARGE -> 24f
            DimenToken.PADDING_ERROR -> 32f
            DimenToken.PADDING_LOADING -> 8f
            DimenToken.PARALLAX_INTENSITY -> 40f
            DimenToken.MAX_CONTENT_WIDTH -> 600f
            DimenToken.LOADING_INDICATOR_SIZE -> 44f
            DimenToken.LOADING_INDICATOR_STROKE -> 3f
            
            DimenToken.WELCOME_MIN_HEIGHT -> 160f
            DimenToken.SUBJECT_CARD_WIDTH -> 220f
            DimenToken.SUBJECT_CARD_HEIGHT -> 280f
            DimenToken.SUBJECT_CARD_ICON_SIZE -> 90f
            
            DimenToken.BACKGROUND_CELL_WIDTH -> 22f
            DimenToken.BACKGROUND_CELL_HEIGHT -> 22f
        }
    }
}
