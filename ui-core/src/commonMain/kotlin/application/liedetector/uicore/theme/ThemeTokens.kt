package application.liedetector.uicore.theme

import kotlinx.serialization.Serializable

@Serializable
enum class ColorToken {
    PRIMARY,
    SECONDARY,
    BACKGROUND,
    SURFACE,
    ERROR,
    ON_PRIMARY,
    ON_BACKGROUND,
    TEXT_PRIMARY,
    TEXT_SECONDARY,
    ACCENT_STRESS,   // Specialized for our lie detector
    ACCENT_TRUTH
}

@Serializable
enum class TypographyToken {
    HEADER_LARGE,
    HEADER_MEDIUM,
    BODY_LARGE,
    BODY_MEDIUM,
    CAPTION,
    MONOSPACE        // For technical analysis data
}

@Serializable
enum class DimenToken {
    SPACING_SMALL,
    SPACING_MEDIUM,
    SPACING_LARGE,
    CORNER_RADIUS,
    ICON_SIZE_SMALL,
    ICON_SIZE_MEDIUM
}
