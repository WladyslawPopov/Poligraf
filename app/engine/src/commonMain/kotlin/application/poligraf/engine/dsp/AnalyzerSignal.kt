package application.poligraf.engine.dsp

/**
 * Single source of truth for the displayed signal state.
 * Derived once from [AnalysisResult]/AudioFrame scores, then mapped to UI.
 */
enum class SignalLevel {
    NONE,
    GLOW,
    ANOMALY,
    CRITICAL
}

/**
 * Which metric contributes the most to the current state.
 */
enum class DominantMetric { JITTER, PITCH, RMS }
