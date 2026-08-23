package application.poligraf.engine.dsp

import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Science-based Audio Analyzer for Voice Stress Analysis.
 * Implements RMS, Pitch (F0), Jitter, and Moving Baseline estimation.
 */
object AudioAnalyzer {

    /**
     * Calculates Root Mean Square (RMS) - proxy for volume and aggression.
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
     * Estimates Pitch (F0) using Autocorrelation.
     * Returns frequency in Hz.
     */
    fun estimatePitch(buffer: ShortArray, sampleRate: Int): Float {
        if (buffer.isEmpty()) return 0f
        
        val maxLag = (sampleRate / 50) // Min frequency 50Hz
        val minLag = (sampleRate / 500) // Max frequency 500Hz
        
        var bestLag = -1
        var maxCorr = -1.0
        
        for (lag in minLag..maxLag) {
            var corr = 0.0
            for (i in 0 until (buffer.size - lag)) {
                corr += buffer[i].toDouble() * buffer[i + lag].toDouble()
            }
            if (corr > maxCorr) {
                maxCorr = corr
                bestLag = lag
            }
        }
        
        return if (bestLag > 0) sampleRate.toFloat() / bestLag else 0f
    }

    /**
     * Calculates Jitter - instability of Pitch.
     */
    fun calculateJitter(pitchHistory: List<Float>): Float {
        if (pitchHistory.size < 2) return 0f
        var sumDiff = 0f
        for (i in 1 until pitchHistory.size) {
            sumDiff += abs(pitchHistory[i] - pitchHistory[i - 1])
        }
        val avgPitch = pitchHistory.average().toFloat()
        return if (avgPitch > 10f) sumDiff / (pitchHistory.size - 1) / avgPitch else 0f
    }

    /**
     * Calculates Zero Crossing Rate (ZCR) - useful for noise filtering.
     */
    fun calculateZcr(buffer: ShortArray): Float {
        if (buffer.isEmpty()) return 0f
        var count = 0
        for (i in 1 until buffer.size) {
            if ((buffer[i] >= 0 && buffer[i - 1] < 0) || (buffer[i] < 0 && buffer[i - 1] >= 0)) {
                count++
            }
        }
        return count.toFloat() / buffer.size
    }

    /**
     * Calculates Stress Score based on deviation from baseline.
     */
    fun calculateStressScore(
        rms: Float,
        pitch: Float,
        jitter: Float,
        baselineRms: Float,
        baselinePitch: Float
    ): Float {
        if (pitch < 50f) return 0f // No voice detected
        
        // Pitch deviation (Stress/Spasms)
        val pitchDev = if (baselinePitch > 0) abs(pitch - baselinePitch) / baselinePitch else 0f
        
        // RMS deviation (Aggression/Pressure)
        val rmsDev = if (baselineRms > 0) (rms - baselineRms).coerceAtLeast(0f) / baselineRms else 0f
        
        // Jitter (Fear/Micro-tremor)
        val jitterScore = jitter * 20f // Empirical multiplier
        
        // Weighted sum
        val score = (pitchDev * 0.4f) + (rmsDev * 0.3f) + (jitterScore * 0.3f)
        
        return score.coerceIn(0f, 1f)
    }

    /**
     * Helper to maintain a moving average for calibration.
     */
    class MovingBaseline(val windowSize: Int = 100) {
        private val rmsHistory = mutableListOf<Float>()
        private val pitchHistory = mutableListOf<Float>()

        fun add(rms: Float, pitch: Float) {
            if (rms > 0.001f) {
                rmsHistory.add(rms)
                if (rmsHistory.size > windowSize) rmsHistory.removeAt(0)
            }
            if (pitch > 50f) {
                pitchHistory.add(pitch)
                if (pitchHistory.size > windowSize) pitchHistory.removeAt(0)
            }
        }

        fun getAvgRms(): Float = if (rmsHistory.isNotEmpty()) rmsHistory.average().toFloat() else 0.01f
        fun getAvgPitch(): Float = if (pitchHistory.isNotEmpty()) pitchHistory.average().toFloat() else 150f
    }
}
