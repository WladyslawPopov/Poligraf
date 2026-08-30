package application.poligraf.engine.dsp

import kotlin.math.*

/**
 * Science-based Audio Analyzer for Voice Stress Analysis.
 * Implements RMS, Pitch (F0), Jitter, and Statistical Baseline estimation.
 *
 * Instrument 2.1: Balanced Statistical Synthesis.
 */
object AudioAnalyzer {

    /**
     * Calculates Root Mean Square (RMS).
     */
    fun calculateRms(buffer: ShortArray): Float {
        if (buffer.isEmpty()) return 0f
        var sum = 0.0
        for (sample in buffer) {
            sum += (sample.toDouble() * sample.toDouble())
        }
        return sqrt(sum / buffer.size).toFloat() / 32767f
    }

    /**
     * Estimates Pitch (F0) using Normalized Autocorrelation.
     */
    fun estimatePitch(buffer: ShortArray, sampleRate: Int, rms: Float): Float {
        if (buffer.isEmpty() || (rms < 0.002f)) return 0f
        
        val maxLag = (sampleRate / 65)
        val minLag = (sampleRate / 450)
        
        if (buffer.size <= maxLag) return 0f
        
        var energyZero = 0.0
        for (i in 0 until (buffer.size - maxLag)) {
            energyZero += buffer[i].toDouble() * buffer[i].toDouble()
        }
        if (energyZero < 1e-7) return 0f

        var bestLag = -1
        var maxNormalizedCorr = 0.0
        
        for (lag in minLag..maxLag) {
            var corr = 0.0
            var energyLag = 0.0
            val len = buffer.size - lag
            
            for (i in 0 until len) {
                val s1 = buffer[i].toDouble()
                val s2 = buffer[i + lag].toDouble()
                corr += s1 * s2
                energyLag += s2 * s2
            }
            
            val normFactor = sqrt(energyZero * energyLag)
            if (normFactor > 0) {
                val normalizedCorr = corr / normFactor
                if (normalizedCorr > maxNormalizedCorr) {
                    maxNormalizedCorr = normalizedCorr
                    bestLag = lag
                }
            }
        }
        
        return if (bestLag > 0 && maxNormalizedCorr > 0.45) {
            sampleRate.toFloat() / bestLag
        } else {
            0f
        }
    }

    /**
     * Calculates Jitter (relative frequency perturbation in %).
     */
    fun calculateJitter(pitchHistory: List<Float>): Float {
        val validPitches = pitchHistory.filter { it > 50f }
        if (validPitches.size < 4) return 0f
        
        var sumDiff = 0f
        for (i in 1 until validPitches.size) {
            sumDiff += abs(validPitches[i] - validPitches[i - 1])
        }
        val avgPitch = validPitches.average().toFloat()
        
        val rawJitter = (sumDiff / (validPitches.size - 1)) / avgPitch
        return (rawJitter * 100f).coerceIn(0f, 100f)
    }

    /**
     * Calculates Balanced Stress Score using Tiered Z-Score Gating.
     * Walks the line between detection and stability.
     */
    fun calculateAdvancedAnalysis(
        rms: Float,
        pitch: Float,
        jitter: Float,
        baseline: MovingBaseline
    ): AnalysisResult {
        // Visuals start after 10 samples (1 second) for "instant life" feel.
        // But markers (isAnomaly) only start after full synthesis (80 samples) for accuracy.
        if (baseline.getSampleCount() < 10) {
            return AnalysisResult(
                stressScore = 0f,
                jitterScore = 0f,
                pitchScore = 0f,
                rmsScore = 0f,
                isAnomaly = false,
                isVisualAnomaly = false,
                confidence = 0f
            )
        }

        // 1. Logarithmic Pitch Deviation (Semitones)
        val pitchRef = baseline.getAvgPitch()
        val pitchDiffSemitones = if (pitch > 50f && pitchRef > 50f) {
            abs(12.0 * log2((pitch / pitchRef).toDouble())).toFloat()
        } else 0f
        
        // 2. Logarithmic Volume (dB)
        val rmsRef = baseline.getAvgRms()
        // Gating silence: only compute increase relative to baseline if current rms is above silence and above baseline
        val rmsDiffDb = if (rms > 0.005f && rmsRef > 0.0001f && rms > rmsRef) {
            abs(20.0 * log10((rms / rmsRef).toDouble())).toFloat()
        } else 0f

        // 3. Z-Score Gating
        // Floor negative Z-scores (we only care about stress/increase)
        val jitterZ = if (jitter > 0.1f) max(0f, (jitter - baseline.getMeanJitter()) / max(baseline.getStdJitter(), 0.5f)) else 0f
        val pitchZ = if (pitch > 50f) max(0f, (pitchDiffSemitones) / max(baseline.getStdPitchSemitones(), 0.5f)) else 0f
        val rmsZ = if (rms > 0.005f) max(0f, (rmsDiffDb) / max(baseline.getStdRmsDb(), 1.0f)) else 0f

        // Balanced weights Instrument 2.6: Exactly 1/3 each
        val totalZ = (jitterZ * 0.33f) + (pitchZ * 0.34f) + (rmsZ * 0.33f)
        
        // Thresholds (Fine-tuned sensitivity):
        // Visual Glow (1.0 sigma) - Responsive feedback
        // Semantic Markers (1.75 sigma) - Balanced anomaly detection
        // Critical Outburst (3.5 sigma)
        
        val isVisualAnomaly = totalZ > 1.0f
        val isAnomaly = baseline.isSynthesized() && totalZ > 1.75f
        val isCritical = baseline.isSynthesized() && totalZ > 3.5f

        // Normalize Z-Score to 0..1 range for UI meters
        // 0.0 (baseline) -> 0.0
        // 2.2 (anomaly threshold) -> 0.5
        // 5.0+ (extreme) -> 1.0
        val jitterScore = if (jitterZ.isNaN() || jitterZ.isInfinite()) 0f else (jitterZ / 5.0f).coerceIn(0f, 1f)
        val pitchScore = if (pitchZ.isNaN() || pitchZ.isInfinite()) 0f else (pitchScoreScaling(pitchZ)).coerceIn(0f, 1f)
        val rmsScore = if (rmsZ.isNaN() || rmsZ.isInfinite()) 0f else (rmsZ / 5.0f).coerceIn(0f, 1f)
        
        val stressScore = if (totalZ.isNaN() || totalZ.isInfinite()) 0f else (totalZ / 5.0f).coerceIn(0f, 1f)
        
        // Confidence increases as baseline stabilizes
        val progress = baseline.getSynthesisProgress()
        val confidence = if (progress.isNaN() || progress.isInfinite()) 0f else progress.coerceIn(0f, 1f)

        return AnalysisResult(
            stressScore = stressScore,
            jitterScore = jitterScore,
            pitchScore = pitchScore,
            rmsScore = rmsScore,
            isAnomaly = isAnomaly,
            isVisualAnomaly = isVisualAnomaly,
            isCritical = isCritical,
            confidence = confidence,
            zScore = totalZ
        )
    }

    private fun pitchScoreScaling(pitchZ: Float): Float {
        return (pitchZ / 5.0f)
    }

    data class AnalysisResult(
        val stressScore: Float,
        val jitterScore: Float,
        val pitchScore: Float,
        val rmsScore: Float,
        val isAnomaly: Boolean,
        val isVisualAnomaly: Boolean,
        val isCritical: Boolean = false,
        val confidence: Float,
        val zScore: Float = 0f
    )

    /**
     * Dual-Track Statistical Baseline with Outlier Rejection.
     * Continuously tracks ambient noise (RMS) and voice metrics (Pitch, Jitter) in parallel.
     */
    class MovingBaseline(val windowSize: Int = 200) {
        private val rmsHistory = mutableListOf<Float>()
        private val pitchHistory = mutableListOf<Float>()
        private val jitterHistory = mutableListOf<Float>()

        private var totalFrames = 0
        private var voiceFrames = 0

        // Statistical parameters (EMA-tracked)
        private var meanRms = 0.01f
        private var meanPitch = 160f
        private var meanJitter = 1.5f
        
        private var stdRmsDb = 2.0f
        private var stdPitchSemitones = 1.0f
        private var stdJitter = 0.8f

        fun add(rms: Float, pitch: Float, jitter: Float, isAnomalyOutlier: Boolean = false) {
            totalFrames++

            // 1. Ambient noise & volume tracking (continuous in background)
            if (rms > 0.0005f) {
                rmsHistory.add(rms)
                if (rmsHistory.size > windowSize) rmsHistory.removeAt(0)
            }

            // 2. Voice tracking (Pitch & Jitter) with Outlier Rejection:
            // Anomaly/stress spikes don't corrupt the long-term calibration baseline
            val hasVoice = pitch > 60f && pitch < 500f
            if (hasVoice) {
                voiceFrames++
                if (!isAnomalyOutlier) {
                    pitchHistory.add(pitch)
                    if (pitchHistory.size > windowSize) pitchHistory.removeAt(0)

                    if (jitter > 0.05f) {
                        jitterHistory.add(jitter)
                        if (jitterHistory.size > windowSize) jitterHistory.removeAt(0)
                    }
                }
            }
            
            updateStats()
        }

        private fun updateStats() {
            if (rmsHistory.isNotEmpty()) {
                meanRms = rmsHistory.average().toFloat()
            }
            if (pitchHistory.isNotEmpty()) {
                meanPitch = pitchHistory.average().toFloat()
            }
            if (jitterHistory.isNotEmpty()) {
                meanJitter = jitterHistory.average().toFloat()
            }

            // Calculate standard deviations for Z-score gating
            if (rmsHistory.size >= 10) {
                stdRmsDb = sqrt(rmsHistory.map { 
                    val db = abs(20.0 * log10((it / meanRms.coerceAtLeast(0.0001f)).toDouble())).toFloat()
                    db * db 
                }.average()).toFloat().coerceIn(1.0f, 10.0f)
            }

            if (pitchHistory.size >= 10) {
                stdPitchSemitones = sqrt(pitchHistory.map {
                    val semi = abs(12.0 * log2((it / meanPitch.coerceAtLeast(50f)).toDouble())).toFloat()
                    semi * semi
                }.average()).toFloat().coerceIn(0.5f, 6.0f)
            }

            if (jitterHistory.size >= 10) {
                stdJitter = sqrt(jitterHistory.map {
                    (it - meanJitter) * (it - meanJitter)
                }.average()).toFloat().coerceIn(0.3f, 5.0f)
            }
        }

        fun getAvgRms() = meanRms
        fun getAvgPitch() = meanPitch
        fun getMeanJitter() = meanJitter
        
        fun getStdRmsDb() = stdRmsDb
        fun getStdPitchSemitones() = stdPitchSemitones
        fun getStdJitter() = stdJitter

        /**
         * Smooth parallel warmup progress: reaches 100% after ~30 voice frames (~1.5-3 sec of speech)
         * and continues to adapt indefinitely.
         */
        fun getSynthesisProgress(): Float {
            val voiceProgress = (voiceFrames / 30f).coerceIn(0f, 1f)
            val ambientProgress = (rmsHistory.size / 20f).coerceIn(0f, 1f)
            return (voiceProgress * 0.7f + ambientProgress * 0.3f).coerceIn(0f, 1f)
        }

        fun isSynthesized(): Boolean = voiceFrames >= 20 && rmsHistory.size >= 10

        fun getSampleCount(): Int = totalFrames

        fun reset() {
            rmsHistory.clear()
            pitchHistory.clear()
            jitterHistory.clear()
            totalFrames = 0
            voiceFrames = 0
            meanRms = 0.01f
            meanPitch = 160f
            meanJitter = 1.5f
            stdRmsDb = 2.0f
            stdPitchSemitones = 1.0f
            stdJitter = 0.8f
        }
    }
}
