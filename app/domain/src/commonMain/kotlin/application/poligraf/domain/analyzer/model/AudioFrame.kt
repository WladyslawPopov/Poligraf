package application.poligraf.domain.analyzer.model

import application.poligraf.domain.analyzer.types.AnalysisStatus
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
)
