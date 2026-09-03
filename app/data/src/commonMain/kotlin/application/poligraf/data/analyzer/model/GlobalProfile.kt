package application.poligraf.data.analyzer.model

/**
 * Statistical profile of the speaker's voice for the entire session history.
 */
internal data class GlobalProfile(
    val rmsMean: Float = 0.035f,
    val rmsStd: Float = 2.5f,
    val rms90: Float = 0.05f,
    val pitchMean: Float = 160f,
    val pitchStd: Float = 1.2f,
    val jitterMean: Float = 1.2f,
    val jitterStd: Float = 0.7f,
    val jitter90: Float = 2.5f,
    val isReady: Boolean = false
)
