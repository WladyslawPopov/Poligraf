package application.poligraf.presentation.analyzer.logic

import application.poligraf.data.analyzer.dsp.AnalyzerProcessor
import application.poligraf.data.analyzer.dsp.QuantumWindowAggregator
import application.poligraf.domain.analyzer.model.AnomalyMarker
import application.poligraf.domain.analyzer.model.AudioFrame
import application.poligraf.domain.analyzer.types.MarkerShape
import application.poligraf.domain.analyzer.types.SensitivityLevel
import application.poligraf.domain.analyzer.types.SignalLevel
import application.poligraf.ui.features.analyzer.models.AnalyzerMarker

/**
 * Pure, lightweight UI holder for the analyzer presentation display state.
 * Holds active frame history, smoothed meter levels for live visualizations,
 * and immutable markers emitted by the Engine.
 */
class AnalyzerSessionController {

    private var markerShape: MarkerShape = MarkerShape.CIRCLE

    private var smoothedJitter = 0f
    private var smoothedPitch = 0f
    private var smoothedRms = 0f
    private var smoothedStress = 0f

    val frameHistory: List<AudioFrame>
        field = mutableListOf<AudioFrame>()
    val timelineMarkers: List<AnalyzerMarker>
        field = mutableListOf<AnalyzerMarker>()

    fun reset() {
        frameHistory.clear()
        timelineMarkers.clear()
        smoothedJitter = 0f
        smoothedPitch = 0f
        smoothedRms = 0f
        smoothedStress = 0f
    }

    fun setMarkerShape(shape: MarkerShape): List<AnalyzerMarker> {
        markerShape = shape
        val updated = timelineMarkers.map { it.copy(shape = shape) }
        timelineMarkers.clear()
        timelineMarkers.addAll(updated)
        return timelineMarkers.toList()
    }

    fun setDomainMarkers(domainMarkers: List<AnomalyMarker>): List<AnalyzerMarker> {
        val existingMap = timelineMarkers.associateBy { it.id }
        val updated = domainMarkers.map { domain ->
            val existing = existingMap[domain.id]
            if (existing != null) {
                if (existing.shape == markerShape && existing.alpha == domain.alpha) {
                    existing
                } else {
                    existing.copy(shape = markerShape, alpha = domain.alpha)
                }
            } else {
                AnalyzerUiMapper.mapDomainMarkerToUi(domain, markerShape)
            }
        }
        if (timelineMarkers != updated) {
            timelineMarkers.clear()
            timelineMarkers.addAll(updated)
        }
        return timelineMarkers.toList()
    }

    fun onLiveFrame(frame: AudioFrame) {
        val lastTimestamp = frameHistory.lastOrNull()?.timestamp ?: -1L
        if (frame.timestamp > lastTimestamp) {
            frameHistory.add(frame)
        }
    }

    fun loadFrames(frames: List<AudioFrame>) {
        frameHistory.clear()
        frameHistory.addAll(frames)
    }

    fun resolveDisplay(
        seekPos: Long?,
        isPaused: Boolean,
        liveFrame: AudioFrame?,
        smooth: Boolean = false,
        quantumWindowMs: Long = 2500L,
        sensitivity: SensitivityLevel = SensitivityLevel.MEDIUM,
    ): AnalyzerDisplaySnapshot {
        val activeFrame = if (isPaused && seekPos != null) {
            AnalyzerProcessor.findClosestFrame(frameHistory, seekPos)
        } else {
            liveFrame ?: frameHistory.lastOrNull()
        }

        val (targetJitter, targetPitch, targetRms) = AnalyzerProcessor.calculateNormalizedMetrics(
            activeFrame
        )
        val rawStress = activeFrame?.stressScore ?: 0f

        if (smooth) {
            smoothedJitter =
                AnalyzerProcessor.applyEmaSmoothing(targetJitter, smoothedJitter, isPaused)
            smoothedPitch =
                AnalyzerProcessor.applyEmaSmoothing(targetPitch, smoothedPitch, isPaused)
            smoothedRms = AnalyzerProcessor.applyEmaSmoothing(targetRms, smoothedRms, isPaused)
            smoothedStress =
                AnalyzerProcessor.applyStressSmoothing(rawStress, smoothedStress, isPaused)
        } else {
            smoothedJitter = targetJitter
            smoothedPitch = targetPitch
            smoothedRms = targetRms
            smoothedStress = rawStress
        }

        val level = AnalyzerProcessor.resolveSignalLevel(activeFrame)
        val dominant = activeFrame?.dominantMetric ?: if (level >= SignalLevel.GLOW) {
            AnalyzerProcessor.resolveDominantMetric(activeFrame)
        } else null

        // Calculate Quantum Window aggregated status for the discrete bucket corresponding to active timestamp
        val activeTimestamp = activeFrame?.timestamp ?: 0L
        val qWindowMs = quantumWindowMs.coerceAtLeast(1000L)
        val bucketIndex = activeTimestamp / qWindowMs
        val windowStart = bucketIndex * qWindowMs
        val windowEnd = windowStart + qWindowMs

        val windowFrames = if (frameHistory.isNotEmpty()) {
            frameHistory.filter { it.timestamp in windowStart until windowEnd }
        } else {
            listOfNotNull(activeFrame)
        }

        val quantumAnalysis = QuantumWindowAggregator.aggregateWindow(windowFrames, sensitivity)

        val activeDisplayStatus = AnalyzerUiMapper.mapStatusToToken(quantumAnalysis.primaryStatus)
        val primaryAlpha = quantumAnalysis.primaryAlpha

        val secondaryInterpretationsWithAlpha = AnalyzerUiMapper.mapStatusesToTokensWithAlpha(
            quantumAnalysis.secondaryStatusesWithScores
        ).filter { it.first != activeDisplayStatus }

        val secondaryInterpretations = secondaryInterpretationsWithAlpha.map { it.first }

        return AnalyzerDisplaySnapshot(
            displayFrame = activeFrame,
            jitterLevel = smoothedJitter,
            pitchLevel = smoothedPitch,
            rmsLevel = smoothedRms,
            signalLevel = level,
            dominantMetric = dominant,
            activeInterpretation = activeDisplayStatus,
            primaryAlpha = primaryAlpha,
            secondaryInterpretations = secondaryInterpretations,
            secondaryInterpretationsWithAlpha = secondaryInterpretationsWithAlpha,
        )
    }
}
