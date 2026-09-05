package application.poligraf.presentation.analyzer.logic

import application.poligraf.data.analyzer.dsp.AnalyzerProcessor
import application.poligraf.domain.analyzer.model.AudioFrame
import application.poligraf.domain.analyzer.types.AnalysisStatus
import application.poligraf.domain.analyzer.types.DominantMetric
import application.poligraf.domain.analyzer.types.MarkerShape
import application.poligraf.domain.analyzer.types.SignalLevel
import application.poligraf.ui.features.analyzer.models.AnalyzerMarker
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.StringToken

/**
 * Maps domain analysis statuses and metrics to UI tokens and markers.
 * Honest, deterministic physical status mapping.
 */
object AnalyzerUiMapper {

    fun mapStatusToToken(status: AnalysisStatus): StringToken = when (status) {
        AnalysisStatus.WARMUP -> StringToken.STATUS_WARMUP
        AnalysisStatus.WARMUP_ROOM -> StringToken.STATUS_WARMUP
        AnalysisStatus.CLIPPING -> StringToken.STATUS_CLIPPING
        AnalysisStatus.LOW_SNR -> StringToken.STATUS_LOW_SNR
        AnalysisStatus.CALM -> StringToken.STATUS_CALM
        AnalysisStatus.MILD_FLUCTUATION -> StringToken.STATUS_MILD_FLUCTUATION
        AnalysisStatus.FEAR_SINGLE -> StringToken.STATUS_FEAR_SINGLE
        AnalysisStatus.STRESS_SINGLE -> StringToken.STATUS_STRESS_SINGLE
        AnalysisStatus.PRESSURE_SINGLE -> StringToken.STATUS_PRESSURE_SINGLE
        AnalysisStatus.PITCH_DROP -> StringToken.STATUS_STRESS_SINGLE
        AnalysisStatus.RMS_DROP -> StringToken.STATUS_PRESSURE_SINGLE
        AnalysisStatus.SUBDUED_TREMOR -> StringToken.STATUS_FEAR_SINGLE
        AnalysisStatus.SUBDUED_SPEECH -> StringToken.STATUS_MILD_FLUCTUATION
        AnalysisStatus.PANIC -> StringToken.INTERPRETATION_PANIC
        AnalysisStatus.AGGRESSION -> StringToken.INTERPRETATION_AGGRESSION
        AnalysisStatus.CONFRONTATION -> StringToken.INTERPRETATION_CONFRONTATION
        AnalysisStatus.DISORGANIZATION -> StringToken.INTERPRETATION_DISORGANIZATION
    }

    fun resolveContinuousStatus(frame: AudioFrame?): StringToken {
        if (frame == null) return StringToken.STATUS_CALM
        return mapStatusToToken(frame.status)
    }

    fun determineInterpretation(
        jitterScore: Float,
        pitchScore: Float,
        rmsScore: Float,
    ): StringToken? {
        val acuteStatus = AnalyzerProcessor.determineInterpretationStatus(
            jitterScore, pitchScore, rmsScore
        ) ?: return null
        return mapStatusToToken(acuteStatus)
    }

    fun determineVolatilityStatus(anomalyCount: Int, durationMillis: Long): StringToken {
        val durationMinutes = (durationMillis / 60000.0).coerceAtLeast(0.15)
        val anomaliesPerMinute = anomalyCount / durationMinutes

        return when {
            anomaliesPerMinute <= 1.2 -> StringToken.VOLATILITY_LOW
            anomaliesPerMinute <= 3.2 -> StringToken.VOLATILITY_MEDIUM
            else -> StringToken.VOLATILITY_HIGH
        }
    }

    fun determineConclusionText(
        anomalyCount: Int,
        durationMillis: Long,
        confidence: Float,
    ): StringToken {
        if (confidence < 0.55f) return StringToken.CONCLUSION_UNRELIABLE

        val durationMinutes = (durationMillis / 60000.0).coerceAtLeast(0.15)
        val anomaliesPerMinute = anomalyCount / durationMinutes

        return when {
            anomaliesPerMinute <= 1.5 -> StringToken.CONCLUSION_POSITIVE
            anomaliesPerMinute <= 3.5 -> StringToken.CONCLUSION_NEUTRAL
            else -> StringToken.CONCLUSION_NEGATIVE
        }
    }

    fun determineConclusionColor(conclusionToken: StringToken): ColorToken =
        when (conclusionToken) {
            StringToken.CONCLUSION_POSITIVE -> ColorToken.STATE_SUCCESS
            StringToken.CONCLUSION_NEUTRAL -> ColorToken.STATE_WARNING
            else -> ColorToken.STATE_ERROR
        }

    fun determineVolatilityColor(volatilityToken: StringToken): ColorToken =
        when (volatilityToken) {
            StringToken.VOLATILITY_LOW -> ColorToken.STATE_SUCCESS
            StringToken.VOLATILITY_MEDIUM -> ColorToken.STATE_WARNING
            else -> ColorToken.STATE_ERROR
        }

    fun colorForDominant(dominant: DominantMetric): ColorToken = when (dominant) {
        DominantMetric.JITTER -> ColorToken.CHART_JITTER
        DominantMetric.PITCH -> ColorToken.CHART_PITCH
        DominantMetric.RMS -> ColorToken.CHART_RMS
    }

    fun createAnomalyMarker(
        frame: AudioFrame,
        shape: MarkerShape,
        lastMarkerTimestamp: Long,
        signalLevel: SignalLevel,
        dominantMetric: DominantMetric?,
    ): AnalyzerMarker? {
        if (signalLevel == SignalLevel.NONE) return null

        val clusterWindow =
            if (signalLevel == SignalLevel.GLOW) 1000L else 600L

        if ((frame.timestamp - lastMarkerTimestamp) < clusterWindow) return null

        return AnalyzerMarker(
            id = "m_${frame.timestamp}",
            timestampMillis = frame.timestamp,
            timestampText = AnalyzerProcessor.formatDuration(frame.timestamp),
            colorToken = dominantMetric?.let { colorForDominant(it) } ?: ColorToken.CHART_JITTER,
            isAnomaly = signalLevel != SignalLevel.GLOW,
            shape = shape,
        )
    }
}
