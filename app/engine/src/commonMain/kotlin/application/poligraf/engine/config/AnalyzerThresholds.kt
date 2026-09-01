package application.poligraf.engine.config

/**
 * Single source of truth for all analyzer thresholds and smoothing constants.
 *
 * Science-based Voice Stress Analysis (VSA) configuration.
 */
object AnalyzerThresholds {

    // --- Sigma gating (Stress & Outburst Levels) ---
    // Glow: initial soft deviation; Anomaly: significant stress; Critical: extreme acute stress
    const val GLOW_SIGMA = 1.3f
    const val ANOMALY_SIGMA = 2.2f
    const val CRITICAL_SIGMA = 3.8f

    // --- Z-score -> 0..1 score normalization ---
    const val SCORE_SCALE = 5.0f
    const val GLOW_SCORE = GLOW_SIGMA / SCORE_SCALE       // ≈0.26
    const val ANOMALY_SCORE = ANOMALY_SIGMA / SCORE_SCALE // ≈0.44
    const val CRITICAL_SCORE = CRITICAL_SIGMA / SCORE_SCALE // ≈0.76

    // --- Semantic interpretation thresholds ---
    const val JITTER_INTERPRET = ANOMALY_SCORE
    const val PITCH_INTERPRET = ANOMALY_SCORE
    const val RMS_INTERPRET = ANOMALY_SCORE

    // --- Timeline marker clustering ---
    const val MARKER_CLUSTER_MS = 600L
    const val INTERPRETATION_STICKY_MS = 2500L

    // --- UI smoothing (EMA alpha) ---
    const val SMOOTH_LIVE = 0.18f
    const val SMOOTH_PAUSED = 0.35f
    const val SMOOTH_STRESS_LIVE = 0.22f
    const val SMOOTH_STRESS_PAUSED = 0.40f

    // --- Environmental & Speech Adaptation ---
    const val NOISE_FLOOR_ALPHA = 0.015f      // Much slower tracking for stability
    const val SPEECH_RMS_ALPHA = 0.02f        // Stable tracking of speech volume
    const val PITCH_BASELINE_ALPHA = 0.02f    // Stable tracking of pitch
    const val JITTER_BASELINE_ALPHA = 0.02f   // Stable tracking of jitter
    const val OUTLIER_LEAK_WEIGHT = 0.05f     // Very soft learning for anomalies

    // --- Calibration Phase ---
    const val CALIBRATION_FAST_ALPHA = 0.15f
    const val CALIBRATION_TOTAL_FRAMES = 200    // ~10 seconds
    const val CALIBRATION_VOICE_FRAMES = 60     // ~3 seconds of speech

    // --- Statistical Window & Look-ahead ---
    const val STATS_WINDOW_SIZE = 120         // ~6 seconds of history (at 50ms step)
    const val LOOKAHEAD_WINDOW_MS = 600L      // 600ms look-ahead to verify anomaly context
    const val WARMUP_DURATION_MS = 5000L      // 5 seconds of silent calibration at start
    const val DYNAMIC_HEADROOM_SIGMA = 1.5f   // Extra padding based on variance
    const val GLOBAL_PERCENTILE = 0.90f       // Only top 10% of values are candidates for anomalies
}

