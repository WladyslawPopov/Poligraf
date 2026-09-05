package application.poligraf.data.analyzer.dsp

import application.poligraf.domain.analyzer.types.AnalysisStatus
import application.poligraf.domain.analyzer.types.SensitivityLevel

/**
 * Pure Data-layer resolver evaluating raw DSP scores and dynamic sensitivity thresholds
 * to determine the continuous domain [AnalysisStatus].
 */
internal object AnalysisStatusResolver {

    fun resolve(
        rms: Float,
        jitterScore: Float,
        pitchScore: Float,
        rmsScore: Float,
        timestamp: Long,
        confidence: Float = 1.0f,
        sensitivity: SensitivityLevel = SensitivityLevel.MEDIUM
    ): AnalysisStatus {
        if (timestamp < AnalyzerThresholds.WARMUP_DURATION_MS || confidence < 0.20f) {
            return AnalysisStatus.WARMUP
        }

        if (rms > 0.85f) {
            return AnalysisStatus.CLIPPING
        }

        val interpretThreshold = when (sensitivity) {
            SensitivityLevel.LOW -> 0.40f
            SensitivityLevel.MEDIUM -> 0.35f
            SensitivityLevel.HIGH -> 0.30f
        }

        val glowThreshold = when (sensitivity) {
            SensitivityLevel.LOW -> 0.26f
            SensitivityLevel.MEDIUM -> 0.22f
            SensitivityLevel.HIGH -> 0.18f
        }

        val isJitterHigh = jitterScore >= interpretThreshold
        val isPitchHigh = pitchScore >= interpretThreshold
        val isRmsHigh = rmsScore >= interpretThreshold

        val isPitchLow = pitchScore < 0.03f && pitchScore > 0f
        val isRmsLow = rmsScore < 0.03f && rmsScore > 0f

        return when {
            isJitterHigh && isPitchHigh && isRmsHigh -> AnalysisStatus.DISORGANIZATION
            isJitterHigh && isPitchHigh -> AnalysisStatus.PANIC
            isJitterHigh && isRmsHigh -> AnalysisStatus.AGGRESSION
            isPitchHigh && isRmsHigh -> AnalysisStatus.CONFRONTATION
            isPitchLow && isRmsLow -> AnalysisStatus.SUBDUED_SPEECH
            isRmsLow && isJitterHigh -> AnalysisStatus.SUBDUED_TREMOR
            isJitterHigh -> AnalysisStatus.FEAR_SINGLE
            isPitchHigh -> AnalysisStatus.STRESS_SINGLE
            isRmsHigh -> AnalysisStatus.PRESSURE_SINGLE
            isPitchLow -> AnalysisStatus.PITCH_DROP
            isRmsLow -> AnalysisStatus.RMS_DROP
            jitterScore >= glowThreshold ||
                    pitchScore >= glowThreshold ||
                    rmsScore >= glowThreshold -> AnalysisStatus.MILD_FLUCTUATION

            else -> AnalysisStatus.CALM
        }
    }
}
