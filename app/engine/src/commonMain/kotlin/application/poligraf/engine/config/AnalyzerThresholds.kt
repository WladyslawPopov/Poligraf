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
    const val NOISE_FLOOR_ALPHA = 0.03f       // Slow tracking of background ambient noise
    const val SPEECH_RMS_ALPHA = 0.05f        // Continuous tracking of speaker's average speech volume
    const val PITCH_BASELINE_ALPHA = 0.04f    // Continuous tracking of speaker's fundamental frequency F0
    const val JITTER_BASELINE_ALPHA = 0.04f   // Continuous tracking of speaker's micro-tremor baseline
    const val OUTLIER_LEAK_WEIGHT = 0.15f     // Soft learning rate for anomalies to avoid getting stuck
}

