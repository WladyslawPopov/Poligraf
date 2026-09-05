package application.poligraf.data.analyzer.dsp

import application.poligraf.data.analyzer.model.AcousticMetrics
import application.poligraf.data.analyzer.model.GlobalProfile
import application.poligraf.domain.analyzer.model.AudioFrame
import application.poligraf.domain.analyzer.types.DominantMetric
import application.poligraf.domain.analyzer.types.SensitivityLevel
import application.poligraf.domain.analyzer.types.SignalLevel
import kotlin.math.*

/**
 * Science-based Audio Analyzer for Voice Stress Analysis (VSA).
 * Primary DSP analysis engine with zero-baseline metrics in calm/silence.
 */
internal object AudioAnalyzer {

    fun calculateRms(buffer: ShortArray): Float {
        if (buffer.isEmpty()) return 0f
        var sum = 0.0
        for (sample in buffer) {
            sum += (sample.toDouble() * sample.toDouble())
        }
        return sqrt(sum / buffer.size).toFloat() / 32767f
    }

    fun estimatePitchWithConfidence(buffer: ShortArray, sampleRate: Int, rms: Float): Pair<Float, Float> {
        if (buffer.isEmpty() || (rms < 0.0015f)) return 0f to 0f

        var totalEnergy = 0.0
        var diffEnergy = 0.0
        for (i in 1 until buffer.size) {
            val s = buffer[i].toDouble()
            val diff = s - buffer[i - 1].toDouble()
            totalEnergy += s * s
            diffEnergy += diff * diff
        }
        if (totalEnergy < 1e-6) return 0f to 0f
        val hfRatio = (diffEnergy / totalEnergy).toFloat()

        if (hfRatio < 0.00012f) return 0f to 0f

        val maxLag = (sampleRate / 85)
        val minLag = (sampleRate / 450)
        if (buffer.size <= maxLag) return 0f to 0f

        var energyZero = 0f
        val lenForEnergy = buffer.size - maxLag
        for (i in 0 until lenForEnergy) {
            val s = buffer[i].toFloat()
            energyZero += s * s
        }
        if (energyZero < 1e-7f) return 0f to 0f

        var bestLag = -1
        var maxNormalizedCorr = 0f

        val corrArray = FloatArray(maxLag - minLag + 3)

        for (lag in minLag..maxLag) {
            var corr = 0f
            var energyLag = 0f
            val len = buffer.size - lag

            for (i in 0 until len) {
                val s1 = buffer[i].toFloat()
                val s2 = buffer[i + lag].toFloat()
                corr += s1 * s2
                energyLag += s2 * s2
            }

            if (energyLag > 0) {
                val normFactor = sqrt(energyZero * energyLag)
                val normalizedCorr = corr / normFactor
                val idx = lag - minLag + 1
                if (idx in corrArray.indices) {
                    corrArray[idx] = normalizedCorr
                }
                if (normalizedCorr > maxNormalizedCorr) {
                    maxNormalizedCorr = normalizedCorr
                    bestLag = lag
                }
            }
        }

        if ((bestLag <= minLag) || (bestLag >= maxLag) || (maxNormalizedCorr < 0.30f)) {
            return 0f to 0f
        }

        val bestIdx = bestLag - minLag + 1
        val y1 = corrArray.getOrElse(bestIdx - 1) { maxNormalizedCorr }
        val y2 = maxNormalizedCorr
        val y3 = corrArray.getOrElse(bestIdx + 1) { maxNormalizedCorr }

        val denominator = 2f * (2f * y2 - y1 - y3)
        val delta = if (abs(denominator) > 1e-6f) {
            ((y1 - y3) / denominator).coerceIn(-0.5f, 0.5f)
        } else 0f

        val exactLag = bestLag + delta
        val pitch = if (exactLag > 0f) sampleRate.toFloat() / exactLag else 0f

        return pitch to maxNormalizedCorr.coerceIn(0f, 1f)
    }

    fun calculateJitter(pitchHistory: List<Float>): Float {
        if (pitchHistory.size < 4) return 0f

        var sumDiff = 0f
        var validCount = 0
        var prevPitch = -1f
        var sumPitch = 0f

        for (p in pitchHistory) {
            if (p in 85f..450f) {
                if (prevPitch > 0) {
                    val diff = abs(p - prevPitch)
                    if (diff / prevPitch < 0.25f) {
                        sumDiff += diff
                        validCount++
                    }
                }
                sumPitch += p
                prevPitch = p
            }
        }

        if (validCount < 3) return 0f
        val avgPitch = sumPitch / (validCount + 1)
        if (avgPitch < 10f) return 0f

        val rawJitter = (sumDiff / validCount) / avgPitch
        val jitterPercent = rawJitter * 100f

        return if (jitterPercent > 15.0f) 0f else jitterPercent.coerceIn(0f, 15f)
    }

    fun calculateGlobalProfile(metrics: List<AcousticMetrics>): GlobalProfile {
        if (metrics.size < 10) return GlobalProfile()

        val voiceMetrics = metrics.filter { it.pitch in 85f..450f && it.rms > 0.0015f }
        if (voiceMetrics.size < 5) return GlobalProfile()

        val rmsList = voiceMetrics.map { it.rms }
        val pitchList = voiceMetrics.map { it.pitch }
        val jitterList = voiceMetrics.asSequence()
            .map { it.jitter }
            .filter { it > 0.05f }
            .toList()

        val rmsMean = rmsList.percentile(0.50f)
        val rms90 = rmsList.percentile(AnalyzerThresholds.GLOBAL_PERCENTILE)
        val rmsStd = calculateStdDb(rmsList, rmsMean)

        val pitchMean = pitchList.percentile(0.50f)
        val pitchStd = calculateStdSemitones(pitchList, pitchMean)

        val jitterMean = if (jitterList.isNotEmpty()) jitterList.percentile(0.50f) else 1.2f
        val jitter90 = if (jitterList.isNotEmpty()) jitterList.percentile(AnalyzerThresholds.GLOBAL_PERCENTILE) else 2.5f
        val jitterStd = if (jitterList.size > 2) calculateStd(jitterList, jitterMean) else 0.7f

        return GlobalProfile(
            rmsMean = rmsMean.coerceAtLeast(0.003f),
            rmsStd = rmsStd.coerceIn(1.5f, 5.0f),
            rms90 = rms90,
            pitchMean = pitchMean.coerceIn(85f, 400f),
            pitchStd = pitchStd.coerceIn(0.6f, 4.5f),
            jitterMean = jitterMean.coerceIn(0.2f, 5.0f),
            jitterStd = jitterStd.coerceIn(0.3f, 4.0f),
            jitter90 = jitter90,
            isReady = voiceMetrics.size >= 15
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

    private fun calculateStdDb(values: List<Float>, refMean: Float): Float {
        if (values.size < 2) return 2.5f
        val ref = refMean.coerceAtLeast(0.002f)
        var sumSq = 0.0
        var count = 0
        for (v in values) {
            if (v > 0.001f) {
                val db = (20.0 * log10((v / ref).toDouble())).toFloat()
                sumSq += db * db
                count++
            }
        }
        if (count < 2) return 2.5f
        return sqrt(sumSq / count).toFloat().coerceIn(1.5f, 5.0f)
    }

    private fun calculateStdSemitones(values: List<Float>, refMean: Float): Float {
        if (values.size < 2) return 1.2f
        val ref = refMean.coerceAtLeast(85f)
        var sumSq = 0.0
        var count = 0
        for (v in values) {
            if (v in 85f..450f) {
                val semi = (12.0 * log2((v / ref).toDouble())).toFloat()
                sumSq += semi * semi
                count++
            }
        }
        if (count < 2) return 1.2f
        return sqrt(sumSq / count).toFloat().coerceIn(0.6f, 4.5f)
    }

    fun calculateHonestAnalysis(
        timestamp: Long,
        rms: Float,
        pitch: Float,
        jitter: Float,
        baseline: MovingBaseline,
        globalProfile: GlobalProfile,
        futureAtoms: List<AcousticMetrics>,
        sensitivity: SensitivityLevel = SensitivityLevel.MEDIUM
    ): AudioFrame {
        if (!baseline.isVoice(rms, pitch)) {
            val status = AnalysisStatusResolver.resolve(rms, 0f, 0f, 0f, timestamp, sensitivity = sensitivity)
            return AudioFrame(
                timestamp = timestamp,
                stressScore = 0f,
                jitterScore = 0f,
                pitchScore = 0f,
                rmsScore = 0f,
                isAnomaly = false,
                status = status
            )
        }

        val speechMean = if (globalProfile.isReady) globalProfile.rmsMean else baseline.speechRms
        val stdRms = if (globalProfile.isReady) globalProfile.rmsStd else 2.5f

        val meanPitch = if (globalProfile.isReady) globalProfile.pitchMean else baseline.pitchMean
        val stdPitch = if (globalProfile.isReady) globalProfile.pitchStd else 1.5f

        val meanJitter = if (globalProfile.isReady) globalProfile.jitterMean else baseline.jitterMean
        val stdJitter = if (globalProfile.isReady) globalProfile.jitterStd else 1.0f

        val recentRmsMean =
            if (futureAtoms.isNotEmpty()) futureAtoms.map { it.rms }.average().toFloat()
                .coerceAtLeast(rms) else rms
        val recentPitchVar =
            if (futureAtoms.size > 2) calculateStd(futureAtoms.map { it.pitch }, pitch).coerceIn(
                0.5f,
                10f
            ) else 1f

        val aggressionScale = (recentRmsMean / speechMean).coerceIn(1.0f, 2.5f)
        val instabilityScale = (recentPitchVar / stdPitch).coerceIn(1.0f, 2.0f)
        val pitchJitterDamper = aggressionScale * instabilityScale

        // Universal voice register adaptation (Deep male < 145Hz, High female/soprano > 240Hz)
        val registerDamper = when {
            meanPitch in 85f..145f -> 1.30f
            meanPitch > 240f && meanPitch <= 450f -> 1.25f
            else -> 1.00f
        }

        val effectiveStdPitch = stdPitch * pitchJitterDamper * registerDamper
        val pitchSemitones = if (pitch in 85f..450f && meanPitch in 85f..450f) {
            (12.0 * log2((pitch / meanPitch).toDouble())).toFloat()
        } else 0f
        val pitchZ = max(0f, pitchSemitones) / effectiveStdPitch
        val pitchDropZ = max(0f, -pitchSemitones) / effectiveStdPitch

        val rmsDeviationDb = if (rms > 0.002f && speechMean > 0.002f) {
            (20.0 * log10((rms / speechMean).toDouble())).toFloat()
        } else 0f
        val rmsZ = max(0f, rmsDeviationDb) / stdRms
        val rmsDropZ = max(0f, -rmsDeviationDb) / stdRms

        val effectiveStdJitter = stdJitter * pitchJitterDamper
        val jitterZ = if (jitter > 0.1f) max(0f, jitter - meanJitter) / effectiveStdJitter else 0f

        val isSignificantRms = rms > (speechMean * 1.10f)
        val isSignificantJitter =
            jitter in 0.5f..15.0f && jitter > (meanJitter * 1.05f)

        val effectiveJitterZ = if (isSignificantJitter) jitterZ else jitterZ * 0.3f
        val effectivePitchZ = max(pitchZ, pitchDropZ)
        val effectiveRmsZ = if (isSignificantRms) max(rmsZ, rmsDropZ) else 0f

        val totalZ = effectiveJitterZ * 0.35f + effectivePitchZ * 0.30f + effectiveRmsZ * 0.35f

        val sigmaMultiplier = when (sensitivity) {
            SensitivityLevel.LOW -> 1.30f
            SensitivityLevel.MEDIUM -> 1.00f
            SensitivityLevel.HIGH -> 0.80f
            else -> 1.00f
        }
        val anomalySigma = AnalyzerThresholds.ANOMALY_SIGMA * sigmaMultiplier
        val criticalSigma = AnalyzerThresholds.CRITICAL_SIGMA * sigmaMultiplier
        val glowSigma = AnalyzerThresholds.GLOW_SIGMA * sigmaMultiplier

        val futureVoiceAtoms = futureAtoms.filter { it.pitch in 85f..450f && it.rms > 0.002f }
        val isTransient =
            if (totalZ >= anomalySigma && futureVoiceAtoms.isNotEmpty()) {
                var futureEnergySum = 0f
                for (fAtom in futureVoiceAtoms) {
                    val fPitchSemitones = if (fAtom.pitch in 85f..450f && meanPitch in 85f..450f) {
                        (12.0 * log2((fAtom.pitch / meanPitch).toDouble())).toFloat()
                    } else 0f
                    val fPitchZ = max(0f, fPitchSemitones) / effectiveStdPitch
                    val fRmsDeviationDb = if (fAtom.rms > 0.002f && speechMean > 0.002f) {
                        (20.0 * log10((fAtom.rms / speechMean).toDouble())).toFloat()
                    } else 0f
                    val fRmsZ = max(0f, fRmsDeviationDb) / stdRms
                    val fJitterZ = if (fAtom.jitter > 0.1f) max(
                        0f,
                        fAtom.jitter - meanJitter
                    ) / effectiveStdJitter else 0f

                    futureEnergySum += (fJitterZ * 0.35f + fPitchZ * 0.30f + fRmsZ * 0.35f)
                }
                val futureZ = futureEnergySum / futureVoiceAtoms.size
                futureZ < totalZ * 0.70f
            } else true

        val finalizedZ = if (isTransient) totalZ else totalZ * 0.5f

        val level = when {
            finalizedZ >= criticalSigma -> SignalLevel.CRITICAL
            finalizedZ >= anomalySigma -> SignalLevel.ANOMALY
            finalizedZ >= glowSigma -> SignalLevel.GLOW
            else -> SignalLevel.NONE
        }

        // Zero-Baseline Stress Scores: In calm speech or silence, Z-scores start at 0% and rise on tension
        val jScore = (effectiveJitterZ / AnalyzerThresholds.SCORE_SCALE).coerceIn(0f, 1f)
        val pScore = (effectivePitchZ / AnalyzerThresholds.SCORE_SCALE).coerceIn(0f, 1f)
        val rScore = (effectiveRmsZ / AnalyzerThresholds.SCORE_SCALE).coerceIn(0f, 1f)

        val status = AnalysisStatusResolver.resolve(
            rms = rms,
            jitterScore = jScore,
            pitchScore = pScore,
            rmsScore = rScore,
            timestamp = timestamp,
            sensitivity = sensitivity
        )

        val dominant = when {
            effectiveJitterZ >= effectivePitchZ && effectiveJitterZ >= effectiveRmsZ && effectiveJitterZ > 0.5f -> DominantMetric.JITTER
            effectivePitchZ >= effectiveJitterZ && effectivePitchZ >= effectiveRmsZ && effectivePitchZ > 0.5f -> DominantMetric.PITCH
            effectiveRmsZ > 0.5f -> DominantMetric.RMS
            else -> null
        }

        return AudioFrame(
            timestamp = timestamp,
            stressScore = (finalizedZ / AnalyzerThresholds.SCORE_SCALE).coerceIn(0f, 1f),
            jitterScore = jScore,
            pitchScore = pScore,
            rmsScore = rScore,
            isAnomaly = level == SignalLevel.ANOMALY || level == SignalLevel.CRITICAL,
            status = status,
            dominantMetric = dominant
        )
    }

    private fun List<Float>.percentile(p: Float): Float {
        if (isEmpty()) return 0f
        val sorted = this.sorted()
        val index = (p * (sorted.size - 1)).toInt()
        return sorted[index]
    }
}
