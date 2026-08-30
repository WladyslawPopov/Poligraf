package application.poligraf.domain.model

import kotlinx.serialization.Serializable

/**
 * A quantized unit of analyzed voice data (usually 100ms).
 * Atomized for Instrument 2.4 precision.
 */
@Serializable
data class AudioFrame(
    val timestamp: Long,
    val rms: Float,
    val pitch: Float,
    val jitter: Float,
    val stressScore: Float,
    val jitterScore: Float = 0f,
    val pitchScore: Float = 0f,
    val rmsScore: Float = 0f,
    val isAnomaly: Boolean = false,
    val isCalibrated: Boolean = true,
    val confidence: Float = 1.0f,
    val isCritical: Boolean = false
)
