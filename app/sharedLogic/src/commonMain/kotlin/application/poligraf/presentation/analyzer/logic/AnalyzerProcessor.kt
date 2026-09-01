package application.poligraf.presentation.analyzer.logic

import application.poligraf.domain.model.AudioFrame
import application.poligraf.domain.model.MarkerShape
import application.poligraf.engine.config.AnalyzerThresholds
import application.poligraf.engine.dsp.AudioAnalyzer
import application.poligraf.engine.dsp.DominantMetric
import application.poligraf.engine.dsp.SignalLevel
import application.poligraf.ui.foundation.models.AnalyzerMarker
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.StringToken
import kotlin.math.*

/**
 * Single presentation processor for both LIVE and REVIEW sessions.
 *
 * Every display transform (normalization, smoothing, signal level, interpretation,
 * markers, post-session recalibration) lives here so the two ViewModels never
 * re-implement the same rules.
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

    /**
     * Maps a raw score (0..1) to a UI-friendly intensity (0..1).
     * Uses a non-linear transfer function (power curve) to make subtle indicators
     * more expressive and "playful" in the low-to-mid range.
     */
    fun mapToUiIntensity(score: Float): Float {
        val safeScore = safe(score)
        if (safeScore <= 0f) return 0f
        // Power function: x^0.60 amplifies small values even more for "playful" UI.
        // 0.05 -> 0.16, 0.1 -> 0.25, 0.2 -> 0.38, 0.5 -> 0.66
        return safeScore.pow(0.60f).coerceIn(0f, 1f)
    }

    /**
     * Unified normalization: returns UI-ready intensities (0..1).
     */
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
        val alpha = if (isPaused) AnalyzerThresholds.SMOOTH_PAUSED else AnalyzerThresholds.SMOOTH_LIVE
        return safe((safeTarget * alpha) + (safeCurrent * (1f - alpha)))
    }

    fun applyStressSmoothing(target: Float, current: Float, isPaused: Boolean): Float {
        val safeTarget = safe(target)
        val safeCurrent = safe(current)
        val alpha = if (isPaused) AnalyzerThresholds.SMOOTH_STRESS_PAUSED else AnalyzerThresholds.SMOOTH_STRESS_LIVE
        return safe((safeTarget * alpha) + (safeCurrent * (1f - alpha)))
    }

    /**
     * Resolves the displayed signal level from an [AudioFrame] without re-deriving
     * sigma thresholds anywhere else.
     * Consistently triggers on anomalies, critical levels, or single dominant biomarker spikes.
     */
    fun resolveSignalLevel(frame: AudioFrame?): SignalLevel {
        if (frame == null) return SignalLevel.NONE
        val maxBiomarkerScore = maxOf(safe(frame.jitterScore), safe(frame.pitchScore), safe(frame.rmsScore))
        return when {
            (frame.isCritical || maxBiomarkerScore >= AnalyzerThresholds.CRITICAL_SCORE) -> SignalLevel.CRITICAL
            (frame.isAnomaly || maxBiomarkerScore >= AnalyzerThresholds.ANOMALY_SCORE) -> SignalLevel.ANOMALY
            (safe(frame.stressScore) >= AnalyzerThresholds.GLOW_SCORE || maxBiomarkerScore >= AnalyzerThresholds.GLOW_SCORE) -> SignalLevel.GLOW
            else -> SignalLevel.NONE
        }
    }

    fun resolveDominantMetric(frame: AudioFrame?): DominantMetric {
        if (frame == null) return DominantMetric.JITTER
        val jitter = safe(frame.jitterScore)
        val pitch = safe(frame.pitchScore)
        val rms = safe(frame.rmsScore)
        return when {
            ((jitter >= pitch) && (jitter >= rms)) -> DominantMetric.JITTER
            (pitch >= rms) -> DominantMetric.PITCH
            else -> DominantMetric.RMS
        }
    }

    /**
     * Unified semantic engine: words are triggered by the same Z-score-derived values
     * that drive charts, glow and markers.
     * Evaluates multi-metric combinations as well as dominant isolated spikes.
     */
    fun determineInterpretation(jitterScore: Float, pitchScore: Float, rmsScore: Float): StringToken? {
        val isJitter = safe(jitterScore) >= AnalyzerThresholds.JITTER_INTERPRET
        val isPitch = safe(pitchScore) >= AnalyzerThresholds.PITCH_INTERPRET
        val isRms = safe(rmsScore) >= AnalyzerThresholds.RMS_INTERPRET

        return when {
            // Triple combination: Total disorganization
            isJitter && isPitch && isRms -> StringToken.INTERPRETATION_DISORGANIZATION

            // Double combinations
            isJitter && isPitch && !isRms -> StringToken.INTERPRETATION_PANIC
            isJitter && isRms && !isPitch -> StringToken.INTERPRETATION_AGGRESSION
            isPitch && isRms && !isJitter -> StringToken.INTERPRETATION_CONFRONTATION

            // Single dominant biomarkers (pure acute reaction)
            isJitter && !isPitch && !isRms -> StringToken.LABEL_FEAR
            isPitch && !isJitter && !isRms -> StringToken.LABEL_STRESS
            isRms && !isJitter && !isPitch -> StringToken.LABEL_PRESSURE

            else -> null
        }
    }

    /**
     * Calculates session-level volatility status based on normalized anomaly density per minute.
     * Prevents short sessions from being falsely penalized and long sessions from falsely failing.
     */
    fun determineVolatilityStatus(anomalyCount: Int, durationMillis: Long): StringToken {
        val durationMinutes = (durationMillis / 60000.0).coerceAtLeast(0.15) // at least 9 sec baseline
        val anomaliesPerMinute = anomalyCount / durationMinutes

        return when {
            anomaliesPerMinute <= 1.2 -> StringToken.VOLATILITY_LOW
            anomaliesPerMinute <= 3.2 -> StringToken.VOLATILITY_MEDIUM
            else -> StringToken.VOLATILITY_HIGH
        }
    }

    /**
     * Calculates session conclusion text considering confidence and rate of anomalies.
     */
    fun determineConclusionText(anomalyCount: Int, durationMillis: Long, confidence: Float): StringToken {
        if (confidence < 0.55f) return StringToken.CONCLUSION_UNRELIABLE

        val durationMinutes = (durationMillis / 60000.0).coerceAtLeast(0.15)
        val anomaliesPerMinute = anomalyCount / durationMinutes

        return when {
            anomaliesPerMinute <= 1.5 -> StringToken.CONCLUSION_POSITIVE
            anomaliesPerMinute <= 3.5 -> StringToken.CONCLUSION_NEUTRAL
            else -> StringToken.CONCLUSION_NEGATIVE
        }
    }

    /**
     * Calculates conclusion color.
     */
    fun determineConclusionColor(conclusionToken: StringToken): ColorToken = when (conclusionToken) {
        StringToken.CONCLUSION_POSITIVE -> ColorToken.STATE_SUCCESS
        StringToken.CONCLUSION_NEUTRAL -> ColorToken.STATE_WARNING
        else -> ColorToken.STATE_ERROR
    }

    /**
     * Calculates volatility color.
     */
    fun determineVolatilityColor(volatilityToken: StringToken): ColorToken = when (volatilityToken) {
        StringToken.VOLATILITY_LOW -> ColorToken.STATE_SUCCESS
        StringToken.VOLATILITY_MEDIUM -> ColorToken.STATE_WARNING
        else -> ColorToken.STATE_ERROR
    }


    fun colorForDominant(dominant: DominantMetric): ColorToken = when (dominant) {
        DominantMetric.JITTER -> ColorToken.CHART_JITTER
        DominantMetric.PITCH -> ColorToken.CHART_PITCH
        DominantMetric.RMS -> ColorToken.CHART_RMS
    }

    fun createAnomalyMarker(
        frame: AudioFrame,
        shape: MarkerShape,
        lastMarkerTimestamp: Long,
        signalLevel: SignalLevel,
        dominantMetric: DominantMetric?,
    ): AnalyzerMarker? {
        // Professional Update: Markers now appear for GLOW level too (subtle tension)
        if (signalLevel == SignalLevel.NONE) return null

        // Quantized clustering window groups nearby reactions into one readable marker.
        // For GLOW markers, we can be slightly more selective to avoid clutter.
        val clusterWindow = if (signalLevel == SignalLevel.GLOW) 1000L else AnalyzerThresholds.MARKER_CLUSTER_MS
        
        if ((frame.timestamp - lastMarkerTimestamp) < clusterWindow) return null

        return AnalyzerMarker(
            id = "m_${frame.timestamp}",
            timestampMillis = frame.timestamp,
            timestampText = formatDuration(frame.timestamp),
            colorToken = dominantMetric?.let { colorForDominant(it) } ?: ColorToken.CHART_JITTER,
            isAnomaly = signalLevel != SignalLevel.GLOW, // true only for real anomalies
            shape = shape,
        )
    }

    /**
     * Post-session recalibration: recomputes scores for a full session against a
     * global baseline. Uses the same "Honest" engine as live.
     * Optimized for speed: avoids redundant mappings.
     */
    fun processFrames(frames: List<AudioFrame>): List<AudioFrame> {
        if (frames.isEmpty()) return emptyList()

        // 1. Convert to internal metrics for analysis
        val metrics = frames.map { AudioAnalyzer.AcousticMetrics(it.rms, it.pitch, it.jitter) }

        val globalProfile = AudioAnalyzer.calculateGlobalProfile(metrics)
        val baseline = AudioAnalyzer.MovingBaseline(windowSize = 200)
        
        // Pre-warm baseline
        val warmupCount = metrics.size.coerceAtMost(100)
        for (i in 0 until warmupCount) {
            val m = metrics[i]
            baseline.add(rms = m.rms, pitch = m.pitch, jitter = m.jitter, isAnomalyOutlier = false)
        }

        val lookAheadCount = (AnalyzerThresholds.LOOKAHEAD_WINDOW_MS / 50).toInt()

        return frames.mapIndexed { index, raw ->
            val futureEnd = (index + 1 + lookAheadCount).coerceAtMost(metrics.size)
            val futureAtoms = if (index + 1 < futureEnd) {
                metrics.subList(index + 1, futureEnd)
            } else emptyList()

            val result = AudioAnalyzer.calculateHonestAnalysis(
                rms = raw.rms,
                pitch = raw.pitch,
                jitter = raw.jitter,
                baseline = baseline,
                globalProfile = globalProfile,
                futureAtoms = futureAtoms,
                isWarmup = raw.timestamp < AnalyzerThresholds.WARMUP_DURATION_MS,
            )
            
            baseline.add(raw.rms, raw.pitch, raw.jitter, result.isAnomaly)

            raw.copy(
                stressScore = result.stressScore,
                jitterScore = result.jitterScore,
                pitchScore = result.pitchScore,
                rmsScore = result.rmsScore,
                isAnomaly = result.isAnomaly,
                isCalibrated = true,
                confidence = result.confidence,
                isCritical = result.isCritical
            )
        }
    }


    private fun safe(value: Float): Float =
        if (value.isNaN() || value.isInfinite()) 0f else value
}
