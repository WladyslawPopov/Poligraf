package application.poligraf.domain.analyzer.model

import application.poligraf.domain.analyzer.types.AnalysisStatus
import application.poligraf.domain.analyzer.types.DominantMetric
import kotlinx.serialization.Serializable

/**
 * Pure domain model representing an immutable timeline anomaly marker emitted by the Engine.
 */
@Serializable
data class AnomalyMarker(
    val id: String,
    val timestampMillis: Long,
    val status: AnalysisStatus,
    val dominantMetric: DominantMetric,
    val isFullAnomaly: Boolean = false,
    val alpha: Float = 1.0f,
)
