package application.liedetector.uicore.theme

import kotlinx.serialization.Serializable

@Serializable
enum class StringToken {
    WELCOME_TITLE,
    WELCOME_SUBTITLE,
    START_INVESTIGATION,
    DRAWER_SETTINGS,
    DRAWER_DARK_MODE,
    
    // Error States
    ERROR_NO_INTERNET_TITLE,
    ERROR_NO_INTERNET_MSG,
    ERROR_SERVER_TITLE,
    ERROR_SERVER_MSG,
    ERROR_UNKNOWN_TITLE,
    ERROR_UNKNOWN_MSG,
    ERROR_RETRY,
    
    // Toast Notifications
    TOAST_AUTH_SUCCESS,
    TOAST_AUTH_FAILED,
    TOAST_GENERIC_WARNING
}

@Serializable
enum class ErrorType {
    NO_INTERNET,
    SERVER_UNAVAILABLE,
    UNAUTHORIZED,
    UNKNOWN
}

@Serializable
enum class ToastType {
    SUCCESS,
    WARNING,
    ERROR
}

@Serializable
data class ToastState(
    val messageToken: StringToken? = null,
    val messageRaw: String? = null,
    val type: ToastType
)

@Serializable
enum class ColorToken {
    // Concrete Palette
    BACKGROUND,      // Deep Anthracite
    SURFACE,         // Light Concrete
    SURFACE_VARIANT, // Subtle borders
    GLASS_BASE,      // Base for glass panels
    GLASS_BORDER,    // For thin borders on cards
    
    // Interaction
    PRIMARY,         // Steel / Silver
    ON_PRIMARY,
    ERROR,           // System error
    
    // Semantic (Neon Accents)
    TRUTH,           // Bio-Green Neon
    STRESS,          // Pulse-Red Neon
    ACCENT_ENERGY,   // Cyan aura
    
    // Text
    TEXT_PRIMARY,
    TEXT_SECONDARY,
    TEXT_INVERTED
}

@Serializable
enum class TypographyToken {
    HEADER,
    SUBHEADER,
    BODY,
    CAPTION,
    DATA_NUMERIC // Specialized for percentages/scores
}

@Serializable
enum class DimenToken {
    MAIN_PADDING,    // Standard padding for screens
    WIDGET_SPACING,  // Space between cards
    CORNER_RADIUS,   // "Material 3" or "iOS Rounded"
    ICON_SIZE_NAV,
    HEADER_HEIGHT,
    
    DRAWER_CORNER,
    WIDGET_CORNER,
    BUTTON_HEIGHT,
    
    SPACING_TINY,
    SPACING_SMALL,
    SPACING_MEDIUM,
    SPACING_LARGE,
    
    PARALLAX_INTENSITY
}
