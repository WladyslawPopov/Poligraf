package application.poligraf.engine.dsp

import kotlin.math.sqrt

/**
 * Science-based Audio Analyzer for Voice Stress Analysis.
 * Implements RMS, Pitch (F0), and Jitter estimation.
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
            sumDiff += kotlin.math.abs(pitchHistory[i] - pitchHistory[i - 1])
        }
        val avgPitch = pitchHistory.average().toFloat()
        return if (avgPitch > 0) sumDiff / (pitchHistory.size - 1) / avgPitch else 0f
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
}
