package application.poligraf.engine.dsp

import application.poligraf.engine.config.AnalyzerThresholds
import kotlin.math.*

/**
 * Adaptive Science-based Audio Analyzer for Voice Stress Analysis (VSA).
 * Tracks physiological acoustic biomarkers:
 * 1. Jitter (Micro-tremor / laryngeal muscle tension instability)
 * 2. Fundamental Frequency F0 (Pitch modulation and involuntary pitch drift)
 * 3. Vocal Intensity RMS (Vocal bursts and respiratory tension relative to speech baseline)
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
     * Estimates Pitch (F0) using Normalized Autocorrelation with Energy Gating.
     */
    fun estimatePitch(buffer: ShortArray, sampleRate: Int, rms: Float): Float {
        if (buffer.isEmpty() || rms < 0.003f) return 0f

        // Human vocal range: ~65 Hz (deep male) to 450 Hz (female/child)
        val maxLag = (sampleRate / 65)
        val minLag = (sampleRate / 450)

        if (buffer.size <= maxLag) return 0f

        val energyZero = energyOf(buffer, buffer.size - maxLag)
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

        return if (bestLag > 0 && maxNormalizedCorr > 0.48) {
            sampleRate.toFloat() / bestLag
        } else {
            0f
        }
    }

    private fun energyOf(buffer: ShortArray, length: Int): Double {
        var energy = 0.0
        for (i in 0 until length) {
            energy += buffer[i].toDouble() * buffer[i].toDouble()
        }
        return energy
    }

    /**
     * Calculates Jitter (relative frequency perturbation percentage).
     */
    fun calculateJitter(pitchHistory: List<Float>): Float {
        val validPitches = pitchHistory.filter { it in 60f..500f }
        if (validPitches.size < 4) return 0f

        var sumDiff = 0f
        for (i in 1 until validPitches.size) {
            sumDiff += abs(validPitches[i] - validPitches[i - 1])
        }
        val avgPitch = validPitches.average().toFloat()
        if (avgPitch < 10f) return 0f

        val rawJitter = (sumDiff / (validPitches.size - 1)) / avgPitch
        return (rawJitter * 100f).coerceIn(0f, 100f)
    }

    /**
     * Performs continuous adaptive voice stress analysis.
     */
    fun calculateAdvancedAnalysis(
        rms: Float,
        pitch: Float,
        jitter: Float,
        baseline: MovingBaseline
    ): AnalysisResult {
        val isVoiceActive = baseline.isVoice(rms, pitch)
        val synthesized = baseline.isSynthesized()
        val early = baseline.getVoiceSampleCount() < 15

        val z = computeZScores(rms, pitch, jitter, baseline, isVoiceActive)

        // When there is no active voice, stress response should stay quiescent (not reacting to room silence)
        val effectiveJitterZ = if (isVoiceActive) z.jitterZ else 0f
        val effectivePitchZ = if (isVoiceActive) max(z.pitchZ, z.pitchDropZ) else 0f
        val effectiveRmsZ = if (isVoiceActive) max(z.rmsZ, z.rmsDropZ) else 0f

        // Voice Stress Biomarker weighting:
        // Jitter (40%) + Pitch F0 tension (35%) + Vocal volume burst/choke (25%)
        val totalZ = if (isVoiceActive) {
            effectiveJitterZ * 0.40f + effectivePitchZ * 0.35f + effectiveRmsZ * 0.25f
        } else {
            0f
        }

        val level = resolveSignalLevel(totalZ, synthesized, early)

        val jitterScore = if (effectiveJitterZ.isNaN() || effectiveJitterZ.isInfinite()) 0f
            else (effectiveJitterZ / AnalyzerThresholds.SCORE_SCALE).coerceIn(0f, 1f)
        val pitchScore = if (effectivePitchZ.isNaN() || effectivePitchZ.isInfinite()) 0f
            else (effectivePitchZ / AnalyzerThresholds.SCORE_SCALE).coerceIn(0f, 1f)
        val rmsScore = if (effectiveRmsZ.isNaN() || effectiveRmsZ.isInfinite()) 0f
            else (effectiveRmsZ / AnalyzerThresholds.SCORE_SCALE).coerceIn(0f, 1f)

        val stressScore = if (totalZ.isNaN() || totalZ.isInfinite()) 0f
            else (totalZ / AnalyzerThresholds.SCORE_SCALE).coerceIn(0f, 1f)

        val dominant = resolveDominant(effectiveJitterZ, effectivePitchZ, effectiveRmsZ)

        val progress = baseline.getSynthesisProgress()
        val confidence = if (progress.isNaN() || progress.isInfinite()) 0f else progress.coerceIn(0f, 1f)

        return AnalysisResult(
            stressScore = stressScore,
            jitterScore = jitterScore,
            pitchScore = pitchScore,
            rmsScore = rmsScore,
            level = level,
            dominantMetric = dominant,
            isAnomaly = level == SignalLevel.ANOMALY || level == SignalLevel.CRITICAL,
            isVisualAnomaly = level == SignalLevel.GLOW || level == SignalLevel.ANOMALY || level == SignalLevel.CRITICAL,
            isCritical = level == SignalLevel.CRITICAL,
            confidence = confidence,
            zScore = totalZ
        )
    }

    /**
     * Maps total Z-score to a discrete SignalLevel.
     */
    fun resolveSignalLevel(totalZ: Float, synthesized: Boolean, early: Boolean = false): SignalLevel {
        if (early || totalZ.isNaN() || totalZ.isInfinite()) return SignalLevel.NONE
        return when {
            totalZ >= AnalyzerThresholds.CRITICAL_SIGMA && synthesized -> SignalLevel.CRITICAL
            totalZ >= AnalyzerThresholds.ANOMALY_SIGMA && synthesized -> SignalLevel.ANOMALY
            totalZ >= AnalyzerThresholds.GLOW_SIGMA -> SignalLevel.GLOW
            else -> SignalLevel.NONE
        }
    }

    /**
     * Selects dominant biomarker driving the current state.
     */
    fun resolveDominant(jitterZ: Float, pitchZ: Float, rmsZ: Float): DominantMetric {
        val safeJitter = if (jitterZ.isNaN()) 0f else jitterZ
        val safePitch = if (pitchZ.isNaN()) 0f else pitchZ
        val safeRms = if (rmsZ.isNaN()) 0f else rmsZ
        return when {
            safeJitter >= safePitch && safeJitter >= safeRms -> DominantMetric.JITTER
            safePitch >= safeRms -> DominantMetric.PITCH
            else -> DominantMetric.RMS
        }
    }

    private data class ZScores(
        val jitterZ: Float,
        val pitchZ: Float,
        val pitchDropZ: Float,
        val rmsZ: Float,
        val rmsDropZ: Float
    )

    private fun computeZScores(
        rms: Float,
        pitch: Float,
        jitter: Float,
        baseline: MovingBaseline,
        isVoiceActive: Boolean
    ): ZScores {
        if (!isVoiceActive) {
            return ZScores(0f, 0f, 0f, 0f, 0f)
        }

        // 1. Pitch F0 Deviation (Measured in Semitones relative to speaker's mean F0)
        val meanPitch = baseline.getAvgPitch()
        val pitchSemitones = if (pitch in 60f..500f && meanPitch in 60f..500f) {
            (12.0 * log2((pitch / meanPitch).toDouble())).toFloat()
        } else 0f

        val pitchRise = max(0f, pitchSemitones)
        val pitchDrop = max(0f, -pitchSemitones)
        val stdPitch = baseline.getStdPitchSemitones().coerceAtLeast(0.8f)

        // 2. Vocal Intensity (dB deviation relative to speaker's average speech loudness, NOT room silence)
        val speechMean = baseline.getSpeechRms()
        val rmsDeviationDb = if (rms > 0.003f && speechMean > 0.003f) {
            (20.0 * log10((rms / speechMean).toDouble())).toFloat()
        } else 0f

        val rmsRise = max(0f, rmsDeviationDb)
        val rmsDrop = max(0f, -rmsDeviationDb)
        val stdRms = baseline.getStdRmsDb().coerceAtLeast(1.5f)

        // 3. Jitter Deviation relative to speaker's baseline micro-tremor
        val meanJitter = baseline.getMeanJitter()
        val stdJitter = baseline.getStdJitter().coerceAtLeast(0.5f)
        val jitterDeviation = max(0f, jitter - meanJitter)

        val jitterZ = if (jitter > 0.1f) jitterDeviation / stdJitter else 0f
        val pitchZ = pitchRise / stdPitch
        val pitchDropZ = pitchDrop / stdPitch
        val rmsZ = rmsRise / stdRms
        val rmsDropZ = rmsDrop / stdRms

        return ZScores(
            jitterZ = jitterZ,
            pitchZ = pitchZ,
            pitchDropZ = pitchDropZ,
            rmsZ = rmsZ,
            rmsDropZ = rmsDropZ
        )
    }

    data class AnalysisResult(
        val stressScore: Float,
        val jitterScore: Float,
        val pitchScore: Float,
        val rmsScore: Float,
        val level: SignalLevel = SignalLevel.NONE,
        val dominantMetric: DominantMetric = DominantMetric.JITTER,
        val isAnomaly: Boolean,
        val isVisualAnomaly: Boolean,
        val isCritical: Boolean = false,
        val confidence: Float,
        val zScore: Float = 0f
    )

    /**
     * Dual-Track Continuous Adaptive Baseline.
     *
     * Separately and continuously models:
     * 1. Ambient Background Noise Floor (tracked in pauses).
     * 2. Speaker's Vocal Profile:
     *    - Mean Speech Loudness (RMS)
     *    - Fundamental Pitch (F0)
     *    - Micro-tremor Jitter
     *
     * Supports Soft Learning (leaky outlier adaptation) so the analyzer never gets stuck
     * if the speaker permanently shifts tone or speaking volume.
     */
    class MovingBaseline(val windowSize: Int = 200) {

        private val speechRmsHistory = mutableListOf<Float>()
        private val pitchHistory = mutableListOf<Float>()
        private val jitterHistory = mutableListOf<Float>()

        private var totalFrames = 0
        private var voiceFrames = 0

        private var noiseFloorRms = 0.004f
        private var speechMeanRms = 0.035f

        private var meanPitch = 160f
        private var meanJitter = 1.2f

        private var stdRmsDb = 2.5f
        private var stdPitchSemitones = 1.2f
        private var stdJitter = 0.7f

        fun isVoice(rms: Float, pitch: Float): Boolean {
            val isAboveNoise = rms > (noiseFloorRms * 1.5f).coerceAtLeast(0.005f)
            val hasValidPitch = pitch in 60f..500f
            return isAboveNoise && hasValidPitch
        }

        fun add(rms: Float, pitch: Float, jitter: Float, isAnomalyOutlier: Boolean = false) {
            totalFrames++

            val hasVoice = isVoice(rms, pitch)

            if (!hasVoice) {
                // Adaptive Background Noise Floor (slow tracking during pauses)
                if (rms in 0.0001f..0.04f) {
                    noiseFloorRms = noiseFloorRms * (1f - AnalyzerThresholds.NOISE_FLOOR_ALPHA) +
                        rms * AnalyzerThresholds.NOISE_FLOOR_ALPHA
                }
            } else {
                voiceFrames++

                // Weight factor: nominal frames adapt normally; outliers adapt softly (leaky learning)
                val weight = if (isAnomalyOutlier) AnalyzerThresholds.OUTLIER_LEAK_WEIGHT else 1.0f

                // 1. Speech RMS Adaptive Tracking
                speechRmsHistory.add(rms)
                if (speechRmsHistory.size > windowSize) speechRmsHistory.removeAt(0)
                val effectiveRmsAlpha = AnalyzerThresholds.SPEECH_RMS_ALPHA * weight
                speechMeanRms = speechMeanRms * (1f - effectiveRmsAlpha) + rms * effectiveRmsAlpha

                // 2. Pitch F0 Adaptive Tracking
                pitchHistory.add(pitch)
                if (pitchHistory.size > windowSize) pitchHistory.removeAt(0)
                val effectivePitchAlpha = AnalyzerThresholds.PITCH_BASELINE_ALPHA * weight
                meanPitch = meanPitch * (1f - effectivePitchAlpha) + pitch * effectivePitchAlpha

                // 3. Jitter Adaptive Tracking
                if (jitter > 0.05f) {
                    jitterHistory.add(jitter)
                    if (jitterHistory.size > windowSize) jitterHistory.removeAt(0)
                    val effectiveJitterAlpha = AnalyzerThresholds.JITTER_BASELINE_ALPHA * weight
                    meanJitter = meanJitter * (1f - effectiveJitterAlpha) + jitter * effectiveJitterAlpha
                }
            }

            updateVariances()
        }

        fun addBulk(frames: List<Triple<Float, Float, Float>>) {
            for ((rms, pitch, jitter) in frames) {
                add(rms, pitch, jitter, isAnomalyOutlier = false)
            }
        }

        private fun updateVariances() {
            // Variance computation for speech loudness deviation in dB
            if (speechRmsHistory.size >= 8) {
                val ref = speechMeanRms.coerceAtLeast(0.003f)
                val dbs = speechRmsHistory.map {
                    (20.0 * log10((it / ref).coerceAtLeast(0.01f).toDouble())).toFloat()
                }
                val meanDb = dbs.average().toFloat()
                stdRmsDb = sqrt(dbs.map { (it - meanDb) * (it - meanDb) }.average())
                    .toFloat()
                    .coerceIn(1.5f, 6.0f)
            }

            // Variance computation for pitch deviation in semitones
            if (pitchHistory.size >= 8) {
                val refPitch = meanPitch.coerceAtLeast(60f)
                val semitones = pitchHistory.map {
                    (12.0 * log2((it / refPitch).coerceAtLeast(0.1f).toDouble())).toFloat()
                }
                val meanSemi = semitones.average().toFloat()
                stdPitchSemitones = sqrt(semitones.map { (it - meanSemi) * (it - meanSemi) }.average())
                    .toFloat()
                    .coerceIn(0.8f, 4.5f)
            }

            // Variance computation for jitter
            if (jitterHistory.size >= 8) {
                stdJitter = sqrt(jitterHistory.map {
                    (it - meanJitter) * (it - meanJitter)
                }.average()).toFloat().coerceIn(0.4f, 4.0f)
            }
        }

        fun getAvgPitch() = meanPitch
        fun getMeanJitter() = meanJitter
        fun getNoiseFloorRms() = noiseFloorRms
        fun getSpeechRms() = speechMeanRms
        fun getStdRmsDb() = stdRmsDb
        fun getStdPitchSemitones() = stdPitchSemitones
        fun getStdJitter() = stdJitter

        /**
         * Voice synthesis calibration progress:
         * Requires ~50 voice frames (~2.5-3 sec of clear speech) to fully calibrate baseline,
         * while ambient noise floor also warms up.
         */
        fun getSynthesisProgress(): Float {
            val voiceProgress = (voiceFrames / 50f).coerceIn(0f, 1f)
            val ambientProgress = (totalFrames / 30f).coerceIn(0f, 1f)
            return (voiceProgress * 0.8f + ambientProgress * 0.2f).coerceIn(0f, 1f)
        }

        fun isSynthesized(): Boolean = voiceFrames >= 40 && totalFrames >= 30

        fun getVoiceSampleCount(): Int = voiceFrames

        fun reset() {
            speechRmsHistory.clear()
            pitchHistory.clear()
            jitterHistory.clear()
            totalFrames = 0
            voiceFrames = 0
            noiseFloorRms = 0.004f
            speechMeanRms = 0.035f
            meanPitch = 160f
            meanJitter = 1.2f
            stdRmsDb = 2.5f
            stdPitchSemitones = 1.2f
            stdJitter = 0.7f
        }
    }
}

