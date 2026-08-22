package application.poligraf.uicore.theme.mappers

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import application.poligraf.uicore.theme.tokens.*

/**
 * Central repository for all Design System values.
 * This ensures consistency across Android and iOS.
 */
object ThemeDefaults {

    fun getColor(token: ColorToken, isDark: Boolean): Color {
        return if (isDark) {
            when (token) {
                ColorToken.BACKGROUND -> Color(0xFF121212)
                ColorToken.SURFACE -> Color(0xFF1E1E1E)
                ColorToken.SURFACE_VARIANT -> Color(0xFF2C2C2C)
                ColorToken.GLASS_BASE -> Color(0x1AFFFFFF)
                ColorToken.GLASS_BORDER -> Color(0x33FFFFFF)
                ColorToken.PRIMARY -> Color(0xFFD1D1D1)
                ColorToken.ON_PRIMARY -> Color(0xFF000000)
                ColorToken.TRUTH -> Color(0xFF00E676)
                ColorToken.STRESS -> Color(0xFFFF5252)
                ColorToken.ACCENT_PRIMARY -> Color(0xFF00F2FF)
                ColorToken.ACCENT_ENERGY -> Color(0xFF00B8D4)
                ColorToken.WARNING -> Color(0xFFFFD600)
                ColorToken.ERROR -> Color(0xFFCF6679)
                ColorToken.TEXT_PRIMARY -> Color(0xFFFFFFFF)
                ColorToken.TEXT_SECONDARY -> Color(0xFFA0A0A0)
                ColorToken.TEXT_INVERTED -> Color(0xFF000000)

                ColorToken.RECORDER_WAVEFORM -> Color(0xFFFF3B30)
                ColorToken.RECORDER_WAVEFORM_BACKGROUND -> Color(0xFF1C1C1E)
                ColorToken.RECORDER_TRIM_ZONE -> Color(0x4DFFD600)
                ColorToken.RECORDER_TRIM_HANDLE -> Color(0xFFFFD600)
                ColorToken.RECORDER_PLAYHEAD -> Color(0xFF007AFF)
                ColorToken.RECORDER_CONTROL_REPLACE -> Color(0xFFFF3B30)

                ColorToken.RECORDER_PRIMARY -> Color(0xFFFF3B30)
                ColorToken.RECORDER_SECONDARY -> Color(0xFF007AFF)
                ColorToken.RECORDER_ACCENT -> Color(0xFFFFD600)
                ColorToken.RECORDER_SURFACE -> Color(0xFF1C1C1E)
                ColorToken.RECORDER_RULER_TEXT -> Color(0x99FFFFFF)
            }
        } else {
            when (token) {
                ColorToken.BACKGROUND -> Color(0xFFCBD5E1)
                ColorToken.SURFACE -> Color(0xFFFFFFFF)
                ColorToken.SURFACE_VARIANT -> Color(0xFF94A3B8)
                ColorToken.GLASS_BASE -> Color(0xB3FFFFFF)
                ColorToken.GLASS_BORDER -> Color(0x33000000)
                ColorToken.PRIMARY -> Color(0xFF334155)
                ColorToken.ON_PRIMARY -> Color(0xFFFFFFFF)
                ColorToken.TRUTH -> Color(0xFF16A34A)
                ColorToken.STRESS -> Color(0xFFDC2626)
                ColorToken.ACCENT_PRIMARY -> Color(0xFF0891B2)
                ColorToken.ACCENT_ENERGY -> Color(0xFF22D3EE)
                ColorToken.WARNING -> Color(0xFFF59E0B)
                ColorToken.ERROR -> Color(0xFFB00020)
                ColorToken.TEXT_PRIMARY -> Color(0xFF0F172A)
                ColorToken.TEXT_SECONDARY -> Color(0xFF64748B)
                ColorToken.TEXT_INVERTED -> Color(0xFFFFFFFF)

                ColorToken.RECORDER_WAVEFORM -> Color(0xFFD32F2F)
                ColorToken.RECORDER_WAVEFORM_BACKGROUND -> Color(0xFFF5F5F5)
                ColorToken.RECORDER_TRIM_ZONE -> Color(0x4DFFC107)
                ColorToken.RECORDER_TRIM_HANDLE -> Color(0xFFFFC107)
                ColorToken.RECORDER_PLAYHEAD -> Color(0xFF1976D2)
                ColorToken.RECORDER_CONTROL_REPLACE -> Color(0xFFD32F2F)

                ColorToken.RECORDER_PRIMARY -> Color(0xFFD32F2F)
                ColorToken.RECORDER_SECONDARY -> Color(0xFF1976D2)
                ColorToken.RECORDER_ACCENT -> Color(0xFFFFC107)
                ColorToken.RECORDER_SURFACE -> Color(0xFFF5F5F5)
                ColorToken.RECORDER_RULER_TEXT -> Color(0x99000000)
            }
        }
    }

    fun getDimension(token: DimenToken): Dp {
        return when (token) {
            DimenToken.MAIN_PADDING -> Dp(16f)
            DimenToken.WIDGET_SPACING -> Dp(12f)
            DimenToken.CORNER_RADIUS -> Dp(12f)
            DimenToken.ICON_SIZE_NAV -> Dp(24f)
            DimenToken.ICON_SIZE_SMALL -> Dp(18f)
            DimenToken.ICON_SIZE_TINY -> Dp(14f)
            DimenToken.ICON_SIZE_LARGE -> Dp(38f)
            DimenToken.HEADER_HEIGHT -> Dp(64f)
            DimenToken.DRAWER_CORNER -> Dp(24f)
            DimenToken.WIDGET_CORNER -> Dp(18f)
            DimenToken.BUTTON_HEIGHT -> Dp(56f)
            DimenToken.SPACING_TINY -> Dp(4f)
            DimenToken.SPACING_SMALL -> Dp(8f)
            DimenToken.SPACING_MEDIUM -> Dp(16f)
            DimenToken.SPACING_LARGE -> Dp(24f)
            DimenToken.SPACING_XL -> Dp(32f)
            DimenToken.PADDING_ERROR -> Dp(32f)
            DimenToken.PADDING_LOADING -> Dp(8f)
            DimenToken.PARALLAX_INTENSITY -> Dp(40f)
            DimenToken.MAX_CONTENT_WIDTH -> Dp(600f)
            DimenToken.LOADING_INDICATOR_SIZE -> Dp(44f)
            DimenToken.LOADING_INDICATOR_STROKE -> Dp(3f)
            
            DimenToken.WELCOME_MIN_HEIGHT -> Dp(160f)
            DimenToken.SUBJECT_CARD_WIDTH -> Dp(220f)
            DimenToken.SUBJECT_CARD_HEIGHT -> Dp(280f)
            DimenToken.SUBJECT_CARD_ICON_SIZE -> Dp(90f)
            
            DimenToken.SUBJECT_STORY_WIDTH -> Dp(110f)
            DimenToken.SUBJECT_STORY_HEIGHT -> Dp(140f)
            DimenToken.SUBJECT_ROW_HEIGHT -> Dp(72f)
            DimenToken.AVATAR_SIZE_SMALL -> Dp(48f)
            
            DimenToken.BACKGROUND_CELL_WIDTH -> Dp(22f)
            DimenToken.BACKGROUND_CELL_HEIGHT -> Dp(22f)

            DimenToken.SELECTION_INDICATOR_WIDTH -> Dp(4f)
            DimenToken.CHECKMARK_SIZE_SMALL -> Dp(14f)
            DimenToken.DIVIDER_THICKNESS -> Dp(0.5f)
            DimenToken.DRAWER_WIDTH -> Dp(320f)
            DimenToken.RECORDER_SHEET_PEEK -> Dp(120f)
            DimenToken.RECORDER_DRAG_HANDLE_WIDTH -> Dp(36f)
            DimenToken.RECORDER_DRAG_HANDLE_HEIGHT -> Dp(5f)
            DimenToken.RECORDER_WAVEFORM_STEP -> Dp(4f)
            DimenToken.RECORDER_WAVEFORM_BAR_WIDTH -> Dp(2f)
            DimenToken.RECORDER_WAVEFORM_MIN_HEIGHT -> Dp(2f)
            DimenToken.RECORDER_WAVEFORM_HEIGHT -> Dp(240f)
            DimenToken.RECORDER_CONTROL_SPACING -> Dp(48f)
            DimenToken.RECORDER_RULER_TICK_LARGE -> Dp(6f)
            DimenToken.RECORDER_RULER_TICK_SMALL -> Dp(3f)
            DimenToken.RECORDER_RULER_TEXT_SIZE -> Dp(10f)

            DimenToken.TEXT_SIZE_TITLE_LARGE -> Dp(42f)
            DimenToken.TEXT_SIZE_TITLE_MEDIUM -> Dp(18f)
            DimenToken.TEXT_SIZE_TITLE_SMALL -> Dp(17f)
            DimenToken.TEXT_SIZE_BODY -> Dp(14f)
            DimenToken.TEXT_SIZE_CAPTION -> Dp(13f)
        }
    }
}
