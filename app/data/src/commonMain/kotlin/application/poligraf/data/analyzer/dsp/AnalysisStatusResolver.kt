package application.poligraf.data.analyzer.dsp

import application.poligraf.domain.analyzer.types.AnalysisStatus

/**
 * Pure Data-layer resolver that evaluates raw DSP scores
 * to determine the continuous domain [AnalysisStatus] for an AudioFrame.
 */
internal object AnalysisStatusResolver {

    fun resolve(
        rms: Float,
        jitterScore: Float,
        pitchScore: Float,
        rmsScore: Float,
        timestamp: Long,
        confidence: Float = 1.0f
    ): AnalysisStatus {
        if (timestamp < AnalyzerThresholds.WARMUP_DURATION_MS || confidence < 0.20f) {
            return AnalysisStatus.WARMUP
        }

        if (rms > 0.85f) {
            return AnalysisStatus.CLIPPING
        }

        val isJitter = jitterScore >= AnalyzerThresholds.JITTER_INTERPRET
        val isPitch = pitchScore >= AnalyzerThresholds.PITCH_INTERPRET
        val isRms = rmsScore >= AnalyzerThresholds.RMS_INTERPRET

        return when {
            isJitter && isPitch && isRms -> AnalysisStatus.DISORGANIZATION
            isJitter && isPitch -> AnalysisStatus.PANIC
            isJitter && isRms -> AnalysisStatus.AGGRESSION
            isPitch && isRms -> AnalysisStatus.CONFRONTATION
            isJitter -> AnalysisStatus.FEAR_SINGLE
            isPitch -> AnalysisStatus.STRESS_SINGLE
            isRms -> AnalysisStatus.PRESSURE_SINGLE
            jitterScore >= AnalyzerThresholds.GLOW_SCORE ||
                    pitchScore >= AnalyzerThresholds.GLOW_SCORE ||
                    rmsScore >= AnalyzerThresholds.GLOW_SCORE -> AnalysisStatus.MILD_FLUCTUATION

            else -> AnalysisStatus.CALM
        }
    }
}
