package application.poligraf.domain.analyzer.model

import application.poligraf.domain.analyzer.types.AnalysisStatus
import application.poligraf.domain.analyzer.types.DominantMetric
import kotlinx.serialization.Serializable

/**
 * Single source of truth for UI audio analysis frame.
 * Perfectly structured and clean for immediate rendering.
 */
@Serializable
data class AudioFrame(
    val timestamp: Long,
    val stressScore: Float,
    val jitterScore: Float = 0f,
    val pitchScore: Float = 0f,
    val rmsScore: Float = 0f,
    val isAnomaly: Boolean = false,
    val status: AnalysisStatus = AnalysisStatus.CALM,
    val primaryAlpha: Float = 1.0f,
    val dominantMetric: DominantMetric? = null,
    val secondaryStatuses: List<AnalysisStatus> = emptyList(),
    val secondaryStatusesWithScores: List<Pair<AnalysisStatus, Float>> = emptyList(),
)
