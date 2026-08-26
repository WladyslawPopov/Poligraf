package application.poligraf.engine.io.audio

// Standard constants for Audio Engine across platforms
object AudioConstants {
    const val SAMPLE_RATE_MS = 33L
    const val BITRATE = 128000
    const val SAMPLING_RATE = 44100
    const val WAVEFORM_STEP_MS = 100f
    const val ANALYSIS_WINDOW_MS = 200L

    // UI Smoothing & Normalization
    const val UI_SMOOTHING_LIVE = 0.10f
    const val UI_SMOOTHING_PAUSED = 0.35f
    const val INTERPRETATION_STICKY_MS = 1000L

    // Metric Normalization Thresholds
    const val MAX_EXPECTED_JITTER = 25f
    const val MIN_EXPECTED_PITCH = 80f
    const val MAX_EXPECTED_PITCH = 360f
    const val RMS_VISUAL_AMPLIFIER = 8f
}
