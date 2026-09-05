package application.poligraf.presentation.analyzer.logic

import application.poligraf.data.analyzer.dsp.AnalyzerProcessor
import application.poligraf.domain.analyzer.model.AnomalyMarker
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

    fun mapStatusesToTokens(statuses: List<AnalysisStatus>): List<StringToken> {
        return statuses.map { mapStatusToToken(it) }.distinct()
    }

    fun mapStatusesToTokensWithAlpha(
        statusPairs: List<Pair<AnalysisStatus, Float>>,
    ): List<Pair<StringToken, Float>> {
        return statusPairs.map { (status, score) ->
            mapStatusToToken(status) to score.coerceIn(0.15f, 1.0f)
        }.distinctBy { it.first }
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

    fun determineVolatilityStatus(weightedAnomalyScore: Float, durationMillis: Long): StringToken {
        val totalWindows = (durationMillis / 1000L).coerceAtLeast(1L).toFloat()
        val stressPercent = (weightedAnomalyScore / totalWindows) * 100f

        return when {
            stressPercent <= 12f -> StringToken.VOLATILITY_LOW
            stressPercent <= 28f -> StringToken.VOLATILITY_MEDIUM
            else -> StringToken.VOLATILITY_HIGH
        }
    }

    fun determineConclusionText(
        weightedAnomalyScore: Float,
        durationMillis: Long,
        confidence: Float,
    ): StringToken {
        if (confidence < 0.55f) return StringToken.CONCLUSION_UNRELIABLE

        val totalWindows = (durationMillis / 1000L).coerceAtLeast(1L).toFloat()
        val stressPercent = (weightedAnomalyScore / totalWindows) * 100f

        return when {
            stressPercent <= 12f -> StringToken.CONCLUSION_POSITIVE
            stressPercent <= 28f -> StringToken.CONCLUSION_NEUTRAL
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

    fun mapDomainMarkerToUi(marker: AnomalyMarker, shape: MarkerShape): AnalyzerMarker {
        return AnalyzerMarker(
            id = marker.id,
            timestampMillis = marker.timestampMillis,
            timestampText = AnalyzerProcessor.formatDuration(marker.timestampMillis),
            colorToken = colorForDominant(marker.dominantMetric),
            isAnomaly = marker.isFullAnomaly,
            shape = shape,
            alpha = marker.alpha
        )
    }

    fun createAnomalyMarker(
        frame: AudioFrame,
        shape: MarkerShape,
        lastMarkerTimestamp: Long,
        signalLevel: SignalLevel,
        dominantMetric: DominantMetric?,
    ): AnalyzerMarker? {
        val hasAnomalyStatus =
            frame.status != AnalysisStatus.CALM && frame.status != AnalysisStatus.WARMUP
        val maxScore = maxOf(frame.stressScore, frame.jitterScore, frame.pitchScore, frame.rmsScore)

        if (!hasAnomalyStatus && !frame.isAnomaly && maxScore < 0.18f) return null

        val clusterWindow = 500L
        if ((frame.timestamp - lastMarkerTimestamp) < clusterWindow) return null

        val statusDominant = when (frame.status) {
            AnalysisStatus.STRESS_SINGLE,
            AnalysisStatus.PITCH_DROP,
            AnalysisStatus.PANIC,
            AnalysisStatus.CONFRONTATION,
                -> DominantMetric.PITCH

            AnalysisStatus.PRESSURE_SINGLE,
            AnalysisStatus.RMS_DROP,
            AnalysisStatus.AGGRESSION,
                -> DominantMetric.RMS

            AnalysisStatus.FEAR_SINGLE,
            AnalysisStatus.SUBDUED_TREMOR,
            AnalysisStatus.DISORGANIZATION,
                -> DominantMetric.JITTER

            else -> null
        }

        val effectiveDominant = statusDominant
            ?: dominantMetric
            ?: frame.dominantMetric
            ?: when {
                frame.jitterScore >= frame.pitchScore && frame.jitterScore >= frame.rmsScore * 0.75f && frame.jitterScore > 0f -> DominantMetric.JITTER
                frame.pitchScore >= frame.jitterScore && frame.pitchScore >= frame.rmsScore * 0.75f && frame.pitchScore > 0f -> DominantMetric.PITCH
                frame.rmsScore > 0f -> DominantMetric.RMS
                else -> DominantMetric.RMS
            }

        val markerAlpha = (maxScore * 0.55f + 0.45f).coerceIn(0.45f, 1.0f)

        return AnalyzerMarker(
            id = "m_${frame.timestamp}",
            timestampMillis = frame.timestamp,
            timestampText = AnalyzerProcessor.formatDuration(frame.timestamp),
            colorToken = colorForDominant(effectiveDominant),
            isAnomaly = frame.isAnomaly || hasAnomalyStatus,
            shape = shape,
            alpha = markerAlpha
        )
    }
}
