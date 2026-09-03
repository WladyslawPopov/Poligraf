package application.poligraf.data.analyzer.model

/**
 * Primary 50ms raw audio measurement atom.
 */
internal data class RawAtom(
    val timestamp: Long,
    val rms: Float,
    val pitch: Float,
    val jitter: Float
)
