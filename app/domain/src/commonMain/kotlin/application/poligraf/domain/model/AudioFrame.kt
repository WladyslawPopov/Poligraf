package application.poligraf.domain.model

import kotlinx.serialization.Serializable

/**
 * A quantized unit of analyzed voice data (usually 100ms).
 */
@Serializable
data class AudioFrame(
    val timestamp: Long,
    val rms: Float,
    val pitch: Float,
    val jitter: Float,
    val stressScore: Float,
    val isAnomaly: Boolean = false,
    val isCalibrated: Boolean = true
)
