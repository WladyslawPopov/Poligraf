package application.liedetector.uicore.theme.tokens

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
    ACCENT_PRIMARY,  // Main interaction (Buttons, Tabs)
    ACCENT_ENERGY,   // Background aura / glow
    WARNING,         // Yellow for processing/onboarding
    
    // Text
    TEXT_PRIMARY,
    TEXT_SECONDARY,
    TEXT_INVERTED
}
