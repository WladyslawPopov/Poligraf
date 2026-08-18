package application.poligraf.uicore.theme

import application.poligraf.uicore.theme.tokens.*

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
                ColorToken.WARNING -> "#FFD600"
                ColorToken.ERROR -> "#CF6679"
                ColorToken.TEXT_PRIMARY -> "#FFFFFF"
                ColorToken.TEXT_SECONDARY -> "#A0A0A0"
                ColorToken.TEXT_INVERTED -> "#000000"

                ColorToken.RECORDER_WAVEFORM -> "#FF3B30"
                ColorToken.RECORDER_WAVEFORM_BACKGROUND -> "#1C1C1E"
                ColorToken.RECORDER_TRIM_ZONE -> "#4DFFD600"
                ColorToken.RECORDER_TRIM_HANDLE -> "#FFD600"
                ColorToken.RECORDER_PLAYHEAD -> "#007AFF"
                ColorToken.RECORDER_CONTROL_REPLACE -> "#FF3B30"

                ColorToken.RECORDER_PRIMARY -> "#FF3B30"
                ColorToken.RECORDER_SECONDARY -> "#007AFF"
                ColorToken.RECORDER_ACCENT -> "#FFD600"
                ColorToken.RECORDER_SURFACE -> "#1C1C1E"
                ColorToken.RECORDER_RULER_TEXT -> "#99FFFFFF"
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
                ColorToken.WARNING -> "#F59E0B"
                ColorToken.ERROR -> "#B00020"
                ColorToken.TEXT_PRIMARY -> "#0F172A"
                ColorToken.TEXT_SECONDARY -> "#64748B"
                ColorToken.TEXT_INVERTED -> "#FFFFFF"

                ColorToken.RECORDER_WAVEFORM -> "#D32F2F"
                ColorToken.RECORDER_WAVEFORM_BACKGROUND -> "#F5F5F5"
                ColorToken.RECORDER_TRIM_ZONE -> "#4DFFC107"
                ColorToken.RECORDER_TRIM_HANDLE -> "#FFC107"
                ColorToken.RECORDER_PLAYHEAD -> "#1976D2"
                ColorToken.RECORDER_CONTROL_REPLACE -> "#D32F2F"

                ColorToken.RECORDER_PRIMARY -> "#D32F2F"
                ColorToken.RECORDER_SECONDARY -> "#1976D2"
                ColorToken.RECORDER_ACCENT -> "#FFC107"
                ColorToken.RECORDER_SURFACE -> "#F5F5F5"
                ColorToken.RECORDER_RULER_TEXT -> "#99000000"
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
            DimenToken.ICON_SIZE_TINY -> 14f
            DimenToken.ICON_SIZE_LARGE -> 38f
            DimenToken.HEADER_HEIGHT -> 64f
            DimenToken.DRAWER_CORNER -> 24f
            DimenToken.WIDGET_CORNER -> 18f
            DimenToken.BUTTON_HEIGHT -> 56f
            DimenToken.SPACING_TINY -> 4f
            DimenToken.SPACING_SMALL -> 8f
            DimenToken.SPACING_MEDIUM -> 16f
            DimenToken.SPACING_LARGE -> 24f
            DimenToken.SPACING_XL -> 32f
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
            
            DimenToken.SUBJECT_STORY_WIDTH -> 110f
            DimenToken.SUBJECT_STORY_HEIGHT -> 140f
            DimenToken.SUBJECT_ROW_HEIGHT -> 72f
            DimenToken.AVATAR_SIZE_SMALL -> 48f
            
            DimenToken.BACKGROUND_CELL_WIDTH -> 22f
            DimenToken.BACKGROUND_CELL_HEIGHT -> 22f

            DimenToken.SELECTION_INDICATOR_WIDTH -> 4f
            DimenToken.CHECKMARK_SIZE_SMALL -> 14f
            DimenToken.DIVIDER_THICKNESS -> 0.5f
            DimenToken.DRAWER_WIDTH -> 320f
            DimenToken.RECORDER_SHEET_PEEK -> 120f
            DimenToken.RECORDER_DRAG_HANDLE_WIDTH -> 36f
            DimenToken.RECORDER_DRAG_HANDLE_HEIGHT -> 5f
            DimenToken.RECORDER_WAVEFORM_STEP -> 4f
            DimenToken.RECORDER_WAVEFORM_BAR_WIDTH -> 2f
            DimenToken.RECORDER_WAVEFORM_MIN_HEIGHT -> 2f
            DimenToken.RECORDER_WAVEFORM_HEIGHT -> 240f
            DimenToken.RECORDER_CONTROL_SPACING -> 48f
            DimenToken.RECORDER_RULER_TICK_LARGE -> 6f
            DimenToken.RECORDER_RULER_TICK_SMALL -> 3f
            DimenToken.RECORDER_RULER_TEXT_SIZE -> 10f

            DimenToken.TEXT_SIZE_TITLE_LARGE -> 42f
            DimenToken.TEXT_SIZE_TITLE_MEDIUM -> 18f
            DimenToken.TEXT_SIZE_TITLE_SMALL -> 17f
            DimenToken.TEXT_SIZE_BODY -> 14f
            DimenToken.TEXT_SIZE_CAPTION -> 13f
        }
    }
}
