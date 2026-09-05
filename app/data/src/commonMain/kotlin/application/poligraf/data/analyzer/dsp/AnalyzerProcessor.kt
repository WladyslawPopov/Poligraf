package application.poligraf.data.analyzer.dsp

import application.poligraf.domain.analyzer.model.AudioFrame
import application.poligraf.domain.analyzer.model.QuantumAnalysis
import application.poligraf.domain.analyzer.types.AnalysisStatus
import application.poligraf.domain.analyzer.types.DominantMetric
import application.poligraf.domain.analyzer.types.SensitivityLevel
import application.poligraf.domain.analyzer.types.SignalLevel
import kotlin.math.pow

/**
 * Data/Analysis processor for session frame calculations and smoothing.
 */
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

    fun mapToUiIntensity(score: Float): Float {
        val safeScore = safe(score)
        if (safeScore <= 0f) return 0f
        return safeScore.pow(0.60f).coerceIn(0f, 1f)
    }

    fun calculateNormalizedMetrics(frame: AudioFrame?): Triple<Float, Float, Float> {
        if (frame == null) return Triple(0f, 0f, 0f)
        val jitter = mapToUiIntensity(frame.jitterScore)
        val pitch = mapToUiIntensity(frame.pitchScore)
        val rms = mapToUiIntensity(frame.rmsScore)
        return Triple(jitter, pitch, rms)
    }

    fun applyEmaSmoothing(target: Float, current: Float, isPaused: Boolean): Float {
        val safeTarget = safe(target)
        val safeCurrent = safe(current)
        val alpha =
            if (isPaused) AnalyzerThresholds.SMOOTH_PAUSED else AnalyzerThresholds.SMOOTH_LIVE
        return safe((safeTarget * alpha) + (safeCurrent * (1f - alpha)))
    }

    fun applyStressSmoothing(target: Float, current: Float, isPaused: Boolean): Float {
        val safeTarget = safe(target)
        val safeCurrent = safe(current)
        val alpha =
            if (isPaused) AnalyzerThresholds.SMOOTH_STRESS_PAUSED else AnalyzerThresholds.SMOOTH_STRESS_LIVE
        return safe((safeTarget * alpha) + (safeCurrent * (1f - alpha)))
    }

    fun resolveSignalLevel(frame: AudioFrame?): SignalLevel {
        if (frame == null) return SignalLevel.NONE
        val maxBiomarkerScore =
            maxOf(safe(frame.jitterScore), safe(frame.pitchScore), safe(frame.rmsScore))
        return when {
            maxBiomarkerScore >= AnalyzerThresholds.CRITICAL_SCORE -> SignalLevel.CRITICAL
            (frame.isAnomaly || maxBiomarkerScore >= AnalyzerThresholds.ANOMALY_SCORE) -> SignalLevel.ANOMALY
            (safe(frame.stressScore) >= AnalyzerThresholds.GLOW_SCORE || maxBiomarkerScore >= AnalyzerThresholds.GLOW_SCORE) -> SignalLevel.GLOW
            else -> SignalLevel.NONE
        }
    }

    fun resolveDominantMetric(frame: AudioFrame?): DominantMetric {
        return frame?.dominantMetric ?: DominantMetric.RMS
    }

    fun determineInterpretationStatus(
        jitterScore: Float,
        pitchScore: Float,
        rmsScore: Float,
    ): AnalysisStatus? {
        val isJitter = safe(jitterScore) >= AnalyzerThresholds.JITTER_INTERPRET
        val isPitch = safe(pitchScore) >= AnalyzerThresholds.PITCH_INTERPRET
        val isRms = safe(rmsScore) >= AnalyzerThresholds.RMS_INTERPRET

        return when {
            isJitter && isPitch && isRms -> AnalysisStatus.DISORGANIZATION
            isJitter && isPitch && !isRms -> AnalysisStatus.PANIC
            isJitter && isRms && !isPitch -> AnalysisStatus.AGGRESSION
            isPitch && isRms && !isJitter -> AnalysisStatus.CONFRONTATION
            isJitter && !isPitch && !isRms -> AnalysisStatus.FEAR_SINGLE
            isPitch && !isJitter && !isRms -> AnalysisStatus.STRESS_SINGLE
            isRms && !isJitter && !isPitch -> AnalysisStatus.PRESSURE_SINGLE
            else -> null
        }
    }

    fun aggregateQuantumWindow(
        frames: List<AudioFrame>,
        sensitivity: SensitivityLevel = SensitivityLevel.MEDIUM
    ): QuantumAnalysis = QuantumWindowAggregator.aggregateWindow(frames, sensitivity)

    private fun safe(value: Float): Float =
        if (value.isNaN() || value.isInfinite()) 0f else value
}
