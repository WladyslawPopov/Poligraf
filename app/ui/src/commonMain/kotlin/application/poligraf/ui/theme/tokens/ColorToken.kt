package application.poligraf.ui.theme.tokens

enum class ColorToken {
    // Surface & Backgrounds
    SURFACE_BACKGROUND, // Main app background
    SURFACE_PRIMARY,    // Main cards/containers
    SURFACE_SECONDARY,  // Secondary containers
    SURFACE_VARIANT,    // Subtle borders/separators
    
    // Glass Effects
    GLASS_BASE,
    GLASS_BORDER,

    // Accents & Brand
    ACCENT_PRIMARY,     // Action buttons, active states
    ACCENT_SECONDARY,   // Less prominent actions
    ACCENT_ENERGY,      // Glows, dynamic effects
    
    // Semantic States
    STATE_SUCCESS,
    STATE_ERROR,
    STATE_WARNING,
    STATE_INFO,

    // Text & Content
    TEXT_PRIMARY,
    TEXT_SECONDARY,
    TEXT_INVERTED,
    
    // Feature Specific
    RECORDER_PRIMARY,
    RECORDER_SECONDARY,
    RECORDER_ACCENT,
    RECORDER_SURFACE,
    RECORDER_WAVEFORM,

    // Analyzer Charts
    CHART_JITTER,
    CHART_JITTER_ALARM,
    CHART_PITCH,
    CHART_PITCH_ALARM,
    CHART_RMS,
    CHART_RMS_ALARM,
    CHART_ANOMALY
}
