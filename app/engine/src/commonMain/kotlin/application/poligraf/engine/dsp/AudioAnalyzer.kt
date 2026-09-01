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
     * Optimized for mobile performance (Float math + reduced redundant calculations).
     */
    fun estimatePitchWithConfidence(buffer: ShortArray, sampleRate: Int, rms: Float): Pair<Float, Float> {
        if (buffer.isEmpty() || (rms < 0.002f)) return 0f to 0f

        val maxLag = (sampleRate / 65)
        val minLag = (sampleRate / 450)
        if (buffer.size <= maxLag) return 0f to 0f

        // Pre-calculate base energy
        var energyZero = 0f
        val lenForEnergy = buffer.size - maxLag
        for (i in 0 until lenForEnergy) {
            val s = buffer[i].toFloat()
            energyZero += s * s
        }
        if (energyZero < 1e-7f) return 0f to 0f

        var bestLag = -1
        var maxNormalizedCorr = 0f

        for (lag in minLag..maxLag) {
            var corr = 0f
            var energyLag = 0f
            val len = buffer.size - lag

            // Optimized inner loop
            for (i in 0 until len) {
                val s1 = buffer[i].toFloat()
                val s2 = buffer[i + lag].toFloat()
                corr += s1 * s2
                energyLag += s2 * s2
            }

            if (energyLag > 0) {
                val normFactor = sqrt(energyZero * energyLag)
                val normalizedCorr = corr / normFactor
                if (normalizedCorr > maxNormalizedCorr) {
                    maxNormalizedCorr = normalizedCorr
                    bestLag = lag
                }
            }
        }

        val pitch = if (bestLag > 0 && maxNormalizedCorr > 0.45f) {
            sampleRate.toFloat() / bestLag
        } else 0f
        
        return pitch to maxNormalizedCorr.coerceIn(0f, 1f)
    }

    /**
     * Calculates Jitter (relative frequency perturbation percentage).
     * Professional implementation: avoids allocations and handles micro-tremor stability.
     */
    fun calculateJitter(pitchHistory: List<Float>): Float {
        if (pitchHistory.size < 4) return 0f

        var sumDiff = 0f
        var validCount = 0
        var prevPitch = -1f
        var sumPitch = 0f

        for (p in pitchHistory) {
            if (p in 60f..500f) {
                if (prevPitch > 0) {
                    sumDiff += abs(p - prevPitch)
                    validCount++
                }
                sumPitch += p
                prevPitch = p
            }
        }

        if (validCount < 3) return 0f
        val avgPitch = sumPitch / (validCount + 1)
        if (avgPitch < 10f) return 0f

        val rawJitter = (sumDiff / validCount) / avgPitch
        return (rawJitter * 100f).coerceIn(0f, 100f)
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

    data class AcousticMetrics(
        val rms: Float,
        val pitch: Float,
        val jitter: Float
    )

    /**
     * Statistical profile of the speaker's voice for the entire session.
     */
    data class GlobalProfile(
        val rmsMean: Float = 0.035f,
        val rmsStd: Float = 1.5f,
        val rms90: Float = 0.05f,
        val pitchMean: Float = 160f,
        val pitchStd: Float = 1.2f,
        val jitterMean: Float = 1.2f,
        val jitterStd: Float = 0.7f,
        val jitter90: Float = 2.5f,
        val isReady: Boolean = false
    )

    /**
     * Calculates GlobalProfile from raw session metrics.
     * Only processes segments identified as Voice to avoid "Silence Bias".
     */
    fun calculateGlobalProfile(metrics: List<AcousticMetrics>): GlobalProfile {
        if (metrics.size < 20) return GlobalProfile()

        // 1. Filter out non-voice segments to establish "True Vocal Norm"
        val voiceMetrics = metrics.filter { it.pitch > 40f && it.rms > 0.002f }
        if (voiceMetrics.size < 10) return GlobalProfile()

        val rmsList = voiceMetrics.map { it.rms }
        val pitchList = voiceMetrics.map { it.pitch }
        val jitterList = voiceMetrics.asSequence()
            .map { it.jitter }
            .filter { it > 0.05f }
            .toList()

        // 2. Log-domain Statistics (dB and Semitones)
        val rmsMean = rmsList.average().toFloat()
        val rms90 = rmsList.percentile(AnalyzerThresholds.GLOBAL_PERCENTILE)
        val rmsStd = calculateStdDb(rmsList, rmsMean)

        val pitchMean = pitchList.average().toFloat()
        val pitchStd = calculateStdSemitones(pitchList, pitchMean)

        val jitterMean = if (jitterList.isNotEmpty()) jitterList.average().toFloat() else 1.2f
        val jitter90 = if (jitterList.isNotEmpty()) jitterList.percentile(AnalyzerThresholds.GLOBAL_PERCENTILE) else 2.5f
        val jitterStd = if (jitterList.size > 2) calculateStd(jitterList, jitterMean) else 0.7f

        return GlobalProfile(
            rmsMean = rmsMean,
            rmsStd = rmsStd.coerceIn(1.2f, 6.0f),
            rms90 = rms90,
            pitchMean = pitchMean,
            pitchStd = pitchStd.coerceIn(0.6f, 4.5f),
            jitterMean = jitterMean,
            jitterStd = jitterStd.coerceIn(0.3f, 4.0f),
            jitter90 = jitter90,
            isReady = voiceMetrics.size >= 60
        )
    }

    private fun calculateStd(values: List<Float>, mean: Float): Float {
        if (values.size < 2) return 0f
        var sumSq = 0.0
        for (v in values) {
            val diff = v - mean
            sumSq += diff * diff
        }
        return sqrt(sumSq / values.size).toFloat()
    }

    private fun calculateStdDb(values: List<Float>, mean: Float): Float {
        if (values.size < 2) return 0f
        val ref = mean.coerceAtLeast(0.003f)
        var sumSq = 0.0
        for (v in values) {
            val db = (20.0 * log10((v / ref).coerceAtLeast(0.01f).toDouble())).toFloat()
            val diff = db - 0f 
            sumSq += diff * diff
        }
        return sqrt(sumSq / values.size).toFloat()
    }

    private fun calculateStdSemitones(values: List<Float>, mean: Float): Float {
        if (values.size < 2) return 0f
        val ref = mean.coerceAtLeast(60f)
        var sumSq = 0.0
        for (v in values) {
            val semi = (12.0 * log2((v / ref).coerceAtLeast(0.1f).toDouble())).toFloat()
            val diff = semi - 0f
            sumSq += diff * diff
        }
        return sqrt(sumSq / values.size).toFloat()
    }

    /**
     * "Honest" VSA Analysis:
     * Uses Global Session Profile, Look-ahead buffer and Dynamic Headroom.
     */
    fun calculateHonestAnalysis(
        rms: Float,
        pitch: Float,
        jitter: Float,
        baseline: MovingBaseline,
        globalProfile: GlobalProfile,
        futureAtoms: List<AcousticMetrics>,
        isWarmup: Boolean
    ): AnalysisResult {
        if (isWarmup || !baseline.isVoice(rms, pitch) || !globalProfile.isReady) {
            val progress = baseline.getSynthesisProgress()
            return AnalysisResult(
                stressScore = 0f,
                jitterScore = 0f,
                pitchScore = 0f,
                rmsScore = 0f,
                level = SignalLevel.NONE,
                isAnomaly = false,
                isVisualAnomaly = false,
                confidence = if (isWarmup) 0.1f else progress,
                zScore = 0f
            )
        }

        // --- DYNAMIC HEADROOM ---
        val recentRmsMean = if (futureAtoms.isNotEmpty()) futureAtoms.map { it.rms }.average().toFloat().coerceAtLeast(rms) else rms
        val recentPitchVar = if (futureAtoms.size > 2) calculateStd(futureAtoms.map { it.pitch }, pitch).coerceIn(0.5f, 10f) else 1f
        
        val aggressionScale = (recentRmsMean / globalProfile.rmsMean).coerceIn(1.0f, 2.5f)
        val instabilityScale = (recentPitchVar / globalProfile.pitchStd).coerceIn(1.0f, 2.0f)
        val dynamicDamper = aggressionScale * instabilityScale

        // 1. Z-Score Calculation (Professional Normalized Metrics)
        val meanPitch = globalProfile.pitchMean
        val stdPitch = globalProfile.pitchStd * dynamicDamper 
        val pitchSemitones = if (pitch in 60f..500f && meanPitch in 60f..500f) {
            (12.0 * log2((pitch / meanPitch).toDouble())).toFloat()
        } else 0f
        val pitchZ = max(0f, pitchSemitones) / stdPitch
        val pitchDropZ = max(0f, -pitchSemitones) / stdPitch

        val speechMean = globalProfile.rmsMean
        val stdRms = globalProfile.rmsStd * dynamicDamper 
        val rmsDeviationDb = if (rms > 0.003f && speechMean > 0.003f) {
            (20.0 * log10((rms / speechMean).toDouble())).toFloat()
        } else 0f
        val rmsZ = max(0f, rmsDeviationDb) / stdRms
        val rmsDropZ = max(0f, -rmsDeviationDb) / stdRms

        val meanJitter = globalProfile.jitterMean
        val stdJitter = globalProfile.jitterStd * dynamicDamper
        val jitterZ = if (jitter > 0.1f) max(0f, jitter - meanJitter) / stdJitter else 0f

        // 2. Global Percentile Gating
        val isSignificantRms = rms > (globalProfile.rms90 * aggressionScale)
        val isSignificantJitter = jitter > (globalProfile.jitter90 * instabilityScale)

        // 3. Weighting Biomarkers
        val effectiveJitterZ = if (isSignificantJitter) jitterZ else jitterZ * 0.3f
        val effectivePitchZ = max(pitchZ, pitchDropZ)
        val effectiveRmsZ = if (isSignificantRms) max(rmsZ, rmsDropZ) else 0f

        val totalZ = effectiveJitterZ * 0.40f + effectivePitchZ * 0.35f + effectiveRmsZ * 0.25f

        // 4. Look-ahead Verification
        val futureVoiceAtoms = futureAtoms.filter { it.pitch > 40f && it.rms > 0.003f }
        val isTransient = if (totalZ >= AnalyzerThresholds.ANOMALY_SIGMA && futureVoiceAtoms.isNotEmpty()) {
            var futureEnergySum = 0f
            for (fAtom in futureVoiceAtoms) {
                val fPitchSemitones = if (fAtom.pitch in 60f..500f && meanPitch in 60f..500f) {
                    (12.0 * log2((fAtom.pitch / meanPitch).toDouble())).toFloat()
                } else 0f
                val fPitchZ = max(0f, fPitchSemitones) / stdPitch
                val fRmsDeviationDb = if (fAtom.rms > 0.003f && speechMean > 0.003f) {
                    (20.0 * log10((fAtom.rms / speechMean).toDouble())).toFloat()
                } else 0f
                val fRmsZ = max(0f, fRmsDeviationDb) / stdRms
                val fJitterZ = if (fAtom.jitter > 0.1f) max(0f, fAtom.jitter - meanJitter) / stdJitter else 0f
                
                futureEnergySum += (fJitterZ * 0.4f + fPitchZ * 0.35f + fRmsZ * 0.25f)
            }
            val futureZ = futureEnergySum / futureVoiceAtoms.size
            futureZ < totalZ * 0.70f 
        } else true

        val finalizedZ = if (isTransient) totalZ else totalZ * 0.4f 
        
        val level = when {
            finalizedZ >= AnalyzerThresholds.CRITICAL_SIGMA -> SignalLevel.CRITICAL
            finalizedZ >= AnalyzerThresholds.ANOMALY_SIGMA -> SignalLevel.ANOMALY
            finalizedZ >= AnalyzerThresholds.GLOW_SIGMA -> SignalLevel.GLOW
            else -> SignalLevel.NONE
        }

        return AnalysisResult(
            stressScore = (finalizedZ / AnalyzerThresholds.SCORE_SCALE).coerceIn(0f, 1f),
            jitterScore = (effectiveJitterZ / AnalyzerThresholds.SCORE_SCALE).coerceIn(0f, 1f),
            pitchScore = (effectivePitchZ / AnalyzerThresholds.SCORE_SCALE).coerceIn(0f, 1f),
            rmsScore = (effectiveRmsZ / AnalyzerThresholds.SCORE_SCALE).coerceIn(0f, 1f),
            level = level,
            dominantMetric = resolveDominant(effectiveJitterZ, effectivePitchZ, effectiveRmsZ),
            isAnomaly = level == SignalLevel.ANOMALY || level == SignalLevel.CRITICAL,
            isVisualAnomaly = level != SignalLevel.NONE,
            isCritical = level == SignalLevel.CRITICAL,
            confidence = if (globalProfile.isReady) 1.0f else baseline.getSynthesisProgress(),
            zScore = finalizedZ
        )
    }

    private fun List<Float>.percentile(p: Float): Float {
        if (isEmpty()) return 0f
        val sorted = this.sorted()
        val index = (p * (sorted.size - 1)).toInt()
        return sorted[index]
    }

    private fun resolveDominant(jitterZ: Float, pitchZ: Float, rmsZ: Float): DominantMetric {
        val safeJitter = if (jitterZ.isNaN()) 0f else jitterZ
        val safePitch = if (pitchZ.isNaN()) 0f else pitchZ
        val safeRms = if (rmsZ.isNaN()) 0f else rmsZ
        return when {
            safeJitter >= safePitch && safeJitter >= safeRms -> DominantMetric.JITTER
            safePitch >= safeRms -> DominantMetric.PITCH
            else -> DominantMetric.RMS
        }
    }

    /**
     * Dual-Track Continuous Adaptive Baseline.
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
            val snrMultiplier = if (totalFrames < 20) 1.2f else 1.8f
            val isAboveNoise = rms > (noiseFloorRms * snrMultiplier).coerceAtLeast(0.003f)
            val hasValidPitch = pitch in 60f..500f
            return isAboveNoise && hasValidPitch
        }

        fun add(rms: Float, pitch: Float, jitter: Float, isAnomalyOutlier: Boolean = false) {
            totalFrames++
            val hasVoice = isVoice(rms, pitch)
            val isInitialPhase = totalFrames < AnalyzerThresholds.CALIBRATION_TOTAL_FRAMES
            val baseAlphaMultiplier = if (isInitialPhase) {
                val progress = totalFrames.toFloat() / AnalyzerThresholds.CALIBRATION_TOTAL_FRAMES
                max(1.0f, (AnalyzerThresholds.CALIBRATION_FAST_ALPHA / AnalyzerThresholds.NOISE_FLOOR_ALPHA) * (1f - progress))
            } else 1.0f

            if (!hasVoice) {
                if (rms in 0.0001f..0.1f) {
                    val alpha = (AnalyzerThresholds.NOISE_FLOOR_ALPHA * baseAlphaMultiplier).coerceIn(0.01f, 0.4f)
                    noiseFloorRms = noiseFloorRms * (1f - alpha) + rms * alpha
                }
            } else {
                voiceFrames++
                val weight = if (isAnomalyOutlier) AnalyzerThresholds.OUTLIER_LEAK_WEIGHT else 1.0f
                val effectiveAlphaScale = baseAlphaMultiplier * weight

                speechRmsHistory.add(rms)
                if (speechRmsHistory.size > windowSize) speechRmsHistory.removeAt(0)
                val effectiveRmsAlpha = (AnalyzerThresholds.SPEECH_RMS_ALPHA * effectiveAlphaScale).coerceIn(0.01f, 0.5f)
                speechMeanRms = speechMeanRms * (1f - effectiveRmsAlpha) + rms * effectiveRmsAlpha

                pitchHistory.add(pitch)
                if (pitchHistory.size > windowSize) pitchHistory.removeAt(0)
                val effectivePitchAlpha = (AnalyzerThresholds.PITCH_BASELINE_ALPHA * effectiveAlphaScale).coerceIn(0.01f, 0.5f)
                meanPitch = meanPitch * (1f - effectivePitchAlpha) + pitch * effectivePitchAlpha

                if (jitter > 0.05f) {
                    jitterHistory.add(jitter)
                    if (jitterHistory.size > windowSize) jitterHistory.removeAt(0)
                    val effectiveJitterAlpha = (AnalyzerThresholds.JITTER_BASELINE_ALPHA * effectiveAlphaScale).coerceIn(0.01f, 0.5f)
                    meanJitter = meanJitter * (1f - effectiveJitterAlpha) + jitter * effectiveJitterAlpha
                }
            }
            updateVariances()
        }

        private fun updateVariances() {
            if (speechRmsHistory.size >= 8) {
                val ref = speechMeanRms.coerceAtLeast(0.003f)
                val dbs = speechRmsHistory.map { (20.0 * log10((it / ref).coerceAtLeast(0.01f).toDouble())).toFloat() }
                val meanDb = dbs.average().toFloat()
                stdRmsDb = sqrt(dbs.map { (it - meanDb) * (it - meanDb) }.average()).toFloat().coerceIn(1.5f, 6.0f)
            }
            if (pitchHistory.size >= 8) {
                val refPitch = meanPitch.coerceAtLeast(60f)
                val semitones = pitchHistory.map { (12.0 * log2((it / refPitch).coerceAtLeast(0.1f).toDouble())).toFloat() }
                val meanSemi = semitones.average().toFloat()
                stdPitchSemitones = sqrt(semitones.map { (it - meanSemi) * (it - meanSemi) }.average()).toFloat().coerceIn(0.8f, 4.5f)
            }
            if (jitterHistory.size >= 8) {
                stdJitter = sqrt(jitterHistory.map { (it - meanJitter) * (it - meanJitter) }.average()).toFloat().coerceIn(0.4f, 4.0f)
            }
        }

        fun getSynthesisProgress(): Float {
            val voiceProgress = (voiceFrames / AnalyzerThresholds.CALIBRATION_VOICE_FRAMES.toFloat()).coerceIn(0f, 1f)
            val ambientProgress = (totalFrames / (AnalyzerThresholds.CALIBRATION_TOTAL_FRAMES / 2f)).coerceIn(0f, 1f)
            return (voiceProgress * 0.7f + ambientProgress * 0.3f).coerceIn(0f, 1f)
        }
        fun isSynthesized(): Boolean = (voiceFrames >= (AnalyzerThresholds.CALIBRATION_VOICE_FRAMES * 0.8).toInt()) && (totalFrames >= (AnalyzerThresholds.CALIBRATION_TOTAL_FRAMES * 0.3).toInt())
        fun reset() {
            speechRmsHistory.clear(); pitchHistory.clear(); jitterHistory.clear()
            totalFrames = 0; voiceFrames = 0; noiseFloorRms = 0.004f; speechMeanRms = 0.035f
            meanPitch = 160f; meanJitter = 1.2f; stdRmsDb = 2.5f; stdPitchSemitones = 1.2f; stdJitter = 0.7f
        }
    }
}
