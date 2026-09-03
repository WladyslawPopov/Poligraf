package application.poligraf.domain.analyzer.types

import kotlinx.serialization.Serializable

/**
 * Which metric contributes the most to the current state.
 */
@Serializable
enum class DominantMetric { JITTER, PITCH, RMS }
