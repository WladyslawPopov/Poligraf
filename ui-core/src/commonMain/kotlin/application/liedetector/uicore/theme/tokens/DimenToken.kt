package application.liedetector.uicore.theme.tokens

import kotlinx.serialization.Serializable

@Serializable
enum class DimenToken {
    MAIN_PADDING,    // Standard padding for screens
    WIDGET_SPACING,  // Space between cards
    CORNER_RADIUS,   // "Material 3" or "iOS Rounded"
    ICON_SIZE_NAV,
    ICON_SIZE_SMALL,
    ICON_SIZE_LARGE,
    HEADER_HEIGHT,
    
    DRAWER_CORNER,
    WIDGET_CORNER,
    BUTTON_HEIGHT,
    
    SPACING_TINY,
    SPACING_SMALL,
    SPACING_MEDIUM,
    SPACING_LARGE,
    
    PADDING_ERROR,
    PADDING_LOADING,
    
    PARALLAX_INTENSITY,
    MAX_CONTENT_WIDTH,
    
    LOADING_INDICATOR_SIZE,
    LOADING_INDICATOR_STROKE,
    
    // Feature specific
    WELCOME_MIN_HEIGHT,
    SUBJECT_CARD_WIDTH,
    SUBJECT_CARD_HEIGHT,
    SUBJECT_CARD_ICON_SIZE
}
