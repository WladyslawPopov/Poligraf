package application.poligraf.engine.dsp

import kotlin.math.*

/**
 * Science-based Audio Analyzer for Voice Stress Analysis.
 * Implements RMS, Pitch (F0), Jitter, and Moving Baseline estimation.
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
        if (buffer.isEmpty() || (rms < 0.004f)) return 0f
        
        val maxLag = (sampleRate / 65)
        val minLag = (sampleRate / 450)
        
        if (buffer.size <= maxLag) return 0f
        
        var energyZero = 0.0
        for (i in 0 until (buffer.size - maxLag)) {
            energyZero += buffer[i].toDouble() * buffer[i].toDouble()
        }
        if (energyZero < 1e-6) return 0f

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

    /**
     * Calculates Jitter (relative frequency perturbation in %).
     */
    fun calculateJitter(pitchHistory: List<Float>): Float {
        val validPitches = pitchHistory.filter { it > 50f }
        if (validPitches.size < 5) return 0f
        
        var sumDiff = 0f
        for (i in 1 until validPitches.size) {
            sumDiff += abs(validPitches[i] - validPitches[i - 1])
        }
        val avgPitch = validPitches.average().toFloat()
        
        val rawJitter = (sumDiff / (validPitches.size - 1)) / avgPitch
        return (rawJitter * 100f).coerceIn(0f, 100f)
    }

    /**
     * Calculates Stress Score based on logarithmic deviation from adaptive baseline.
     */
    fun calculateStressScore(
        rms: Float,
        pitch: Float,
        jitter: Float,
        baselineRms: Float,
        baselinePitch: Float,
    ): Float {
        if (pitch < 50f || rms < 0.004f) return 0f
        
        // 1. Pitch Deviation in SEMITONES (Logarithmic)
        // Formula: 12 * log2(f2 / f1)
        // This neutralizes the difference between high/low voices.
        val pitchDiffSemitones = if (baselinePitch > 50f) {
            abs(12.0 * log2((pitch / baselinePitch).toDouble())).toFloat()
        } else 0f
        
        // Typical speech variation is 1-3 semitones. > 4 is high stress.
        val pitchScore = (pitchDiffSemitones / 5f).coerceIn(0f, 1f)
        
        // 2. RMS Deviation in Decibels (dB)
        // We measure how many dB current volume is above/below person's average.
        val rmsDiffDb = if (baselineRms > 0.0001f) {
            abs(20.0 * log10((rms / baselineRms).toDouble())).toFloat()
        } else 0f
        
        // > 6dB increase is a significant "pressure" jump.
        val rmsScore = (rmsDiffDb / 10f).coerceIn(0f, 1f)
        
        // 3. Jitter score (micro-tremor: > 12% is anomalous)
        val jitterScore = (jitter / 20f).coerceIn(0f, 1f)
        
        // Weighted Stress Score (Pitch is most indicative of cognitive stress)
        val score = (pitchScore * 0.5f) + (rmsScore * 0.25f) + (jitterScore * 0.25f)
        return score.coerceIn(0f, 1f)
    }

    /**
     * Adaptive moving baseline that continuously tracks speaker tone and room acoustics.
     */
    class MovingBaseline(val windowSize: Int = 150) {
        private val rmsHistory = mutableListOf<Float>()
        private val pitchHistory = mutableListOf<Float>()

        fun add(rms: Float, pitch: Float) {
            // Continuously adapt RMS (tracks speech volume and room noise)
            if (rms > 0.0005f) {
                rmsHistory.add(rms)
                if (rmsHistory.size > windowSize) rmsHistory.removeAt(0)
            }
            // Continuously adapt Pitch (smoothly slides baseline with speaker's natural tone)
            if (pitch > 65f && pitch < 450f) {
                pitchHistory.add(pitch)
                if (pitchHistory.size > windowSize) pitchHistory.removeAt(0)
            }
        }

        fun getAvgRms(): Float = if (rmsHistory.isNotEmpty()) rmsHistory.average().toFloat() else 0.01f

        fun getAvgPitch(): Float = if (pitchHistory.isNotEmpty()) pitchHistory.average().toFloat() else 160f
        
        fun reset() {
            rmsHistory.clear()
            pitchHistory.clear()
        }

        // Ready after collecting 30 voice samples (~3-4s of speech), then adapts continuously
        fun isCalibrated(): Boolean = rmsHistory.size > 20 && pitchHistory.size > 20
    }
}
