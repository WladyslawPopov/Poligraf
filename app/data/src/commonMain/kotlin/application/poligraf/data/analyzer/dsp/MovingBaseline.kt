package application.poligraf.data.analyzer.dsp

import kotlin.math.log10
import kotlin.math.log2
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Dual-Track Continuous Adaptive Baseline.
 * Separately models background noise floor and speaker's vocal profile.
 */
internal class MovingBaseline(val windowSize: Int = 200) {
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

    val speechRms: Float get() = speechMeanRms
    val pitchMean: Float get() = meanPitch
    val jitterMean: Float get() = meanJitter

    fun isVoice(rms: Float, pitch: Float): Boolean {
        val snrMultiplier = if (totalFrames < 20) 1.2f else 1.8f
        val isAboveNoise = rms > (noiseFloorRms * snrMultiplier).coerceAtLeast(0.003f)
        val hasValidPitch = pitch in 85f..450f
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

            if (jitter in 0.05f..15.0f) {
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
            val refPitch = meanPitch.coerceAtLeast(85f)
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
