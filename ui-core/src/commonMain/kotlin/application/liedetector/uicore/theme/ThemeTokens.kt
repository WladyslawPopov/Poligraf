package application.liedetector.uicore.theme

import kotlinx.serialization.Serializable

@Serializable
enum class ColorToken {
    // Concrete Palette
    BACKGROUND,      // Deep Anthracite / Pure White
    SURFACE,         // Light Concrete
    SURFACE_VARIANT, // For subtle borders/dividers
    
    // Interaction
    PRIMARY,         // Steel / Silver
    ON_PRIMARY,
    ERROR,           // System error
    
    // Semantic (Neon Accents)
    TRUTH,           // Bio-Green Neon
    STRESS,          // Pulse-Red Neon
    
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
    HEADER_HEIGHT
}
