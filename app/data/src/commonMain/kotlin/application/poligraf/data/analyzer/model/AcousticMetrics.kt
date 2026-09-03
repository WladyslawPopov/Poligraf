package application.poligraf.data.analyzer.model

/**
 * Raw physical acoustic metrics calculated for a single audio frame.
 */
internal data class AcousticMetrics(
    val rms: Float,
    val pitch: Float,
    val jitter: Float
)
