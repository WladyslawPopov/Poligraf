package application.poligraf.presentation.analyzer.logic

import application.poligraf.domain.model.AudioFrame
import application.poligraf.domain.model.MarkerShape
import application.poligraf.ui.foundation.models.AnalyzerMarker
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.StringToken

object AnalyzerProcessor {

    fun formatDuration(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    }

    fun findClosestFrame(frames: List<AudioFrame>, seekPos: Long): AudioFrame? {
        if (frames.isEmpty()) return null

        var low = 0
        var high = frames.size - 1

        while (low <= high) {
            val mid = (low + high) / 2
            val midVal = frames[mid].timestamp

            when {
                midVal < seekPos -> low = mid + 1
                midVal > seekPos -> high = mid - 1
                else -> return frames[mid]
            }
        }

        val index = low.coerceIn(0, frames.size - 1)
        return frames[index]
    }

    /**
     * Unified Normalization: Directly returns the pre-computed statistical scores (0..1).
     * This eliminates magic numbers and ensures visualizers match the statistical reality.
     */
    fun calculateNormalizedMetrics(frame: AudioFrame?): Triple<Float, Float, Float> {
        if (frame == null) return Triple(0f, 0f, 0f)
        val jitter = if (frame.jitterScore.isNaN() || frame.jitterScore.isInfinite()) 0f else frame.jitterScore
        val pitch = if (frame.pitchScore.isNaN() || frame.pitchScore.isInfinite()) 0f else frame.pitchScore
        val rms = if (frame.rmsScore.isNaN() || frame.rmsScore.isInfinite()) 0f else frame.rmsScore
        return Triple(jitter, pitch, rms)
    }

    fun applyEmaSmoothing(
        target: Float,
        current: Float,
        isPaused: Boolean
    ): Float {
        val safeTarget = if (target.isNaN() || target.isInfinite()) 0f else target
        val safeCurrent = if (current.isNaN() || current.isInfinite()) 0f else current
        // Professional inertia for "analog" feel
        val alpha = if (isPaused) 0.35f else 0.15f
        val result = (safeTarget * alpha) + (safeCurrent * (1f - alpha))
        return if (result.isNaN() || result.isInfinite()) 0f else result
    }

    /**
     * Unified Semantic Engine: Words are triggered by the same Z-score derived values
     * that drive the charts and markers.
     */
    fun determineInterpretation(jitterScore: Float, pitchScore: Float, rmsScore: Float): StringToken? {
        // Threshold 0.35 corresponds to approx 1.75 sigma
        val isJitter = jitterScore > 0.35f
        val isPitch = pitchScore > 0.40f
        val isRms = rmsScore > 0.35f

        return when {
            isJitter && isPitch && isRms -> StringToken.INTERPRETATION_DISORGANIZATION
            isJitter && isPitch && !isRms -> StringToken.INTERPRETATION_PANIC
            isJitter && isRms && !isPitch -> StringToken.INTERPRETATION_AGGRESSION
            isPitch && isRms && !isJitter -> StringToken.INTERPRETATION_CONFRONTATION
            else -> null
        }
    }

    fun createAnomalyMarker(
        frame: AudioFrame,
        shape: MarkerShape,
        lastMarkerTimestamp: Long
    ): AnalyzerMarker? {
        // Quantized window of 2500ms (2.5 seconds) to cluster closely occurring anomalies
        // into a single representative event without visual overlapping clutter.
        val clusterWindowMs = 2500L
        val isWindowActive = (frame.timestamp - lastMarkerTimestamp) < clusterWindowMs
        
        if (!frame.isAnomaly) return null
        if (isWindowActive) return null

        // Choose dominant color based on highest contributing Z-score
        val dominantColor = when {
            frame.jitterScore > frame.pitchScore && frame.jitterScore > frame.rmsScore -> ColorToken.CHART_JITTER
            frame.pitchScore > frame.rmsScore -> ColorToken.CHART_PITCH
            else -> ColorToken.CHART_RMS
        }

        return AnalyzerMarker(
            id = "m_${frame.timestamp}",
            timestampMillis = frame.timestamp,
            timestampText = formatDuration(frame.timestamp),
            colorToken = dominantColor,
            isAnomaly = true,
            shape = shape
        )
    }
}
