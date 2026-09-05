package application.poligraf.presentation.analyzer.logic

import application.poligraf.data.analyzer.dsp.AnalyzerProcessor
import application.poligraf.domain.analyzer.model.AudioFrame
import application.poligraf.domain.analyzer.types.MarkerShape
import application.poligraf.domain.analyzer.types.SignalLevel
import application.poligraf.engine.utils.nowAsEpochMilliseconds
import application.poligraf.ui.features.analyzer.models.AnalyzerMarker
import application.poligraf.ui.theme.tokens.StringToken

/**
 * Pure, reusable holder for the analyzer presentation display state
 * featuring 2.5s conversational quantum windowing for rock-solid readable status text.
 */
class AnalyzerSessionController {

    private var markerShape: MarkerShape = MarkerShape.CIRCLE

    private var smoothedJitter = 0f
    private var smoothedPitch = 0f
    private var smoothedRms = 0f
    private var smoothedStress = 0f

    private var lastInterpretation: StringToken? = null
    private var interpretationTimestamp = 0L

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
        lastInterpretation = null
        interpretationTimestamp = 0L
    }

    fun setMarkerShape(shape: MarkerShape): List<AnalyzerMarker> {
        markerShape = shape
        val updated = timelineMarkers.map { it.copy(shape = shape) }
        timelineMarkers.clear()
        timelineMarkers.addAll(updated)
        return timelineMarkers.toList()
    }

    fun onLiveFrame(frame: AudioFrame) {
        val lastTimestamp = frameHistory.lastOrNull()?.timestamp ?: -1L
        if (frame.timestamp > lastTimestamp) {
            frameHistory.add(frame)
        }
        appendMarkerIfNeeded(frame)
    }

    fun loadFrames(frames: List<AudioFrame>) {
        frameHistory.clear()
        frameHistory.addAll(frames)
        timelineMarkers.clear()
        frames.forEach { appendMarkerIfNeeded(it) }
    }

    private fun appendMarkerIfNeeded(frame: AudioFrame) {
        if (frame.timestamp < 5000L) return

        val level = AnalyzerProcessor.resolveSignalLevel(frame)
        val dominant = if (level != SignalLevel.NONE) {
            AnalyzerProcessor.resolveDominantMetric(frame)
        } else null
        val lastMarkerTime = timelineMarkers.lastOrNull()?.timestampMillis ?: -10000L
        AnalyzerUiMapper.createAnomalyMarker(
            frame = frame,
            shape = markerShape,
            lastMarkerTimestamp = lastMarkerTime,
            signalLevel = level,
            dominantMetric = dominant
        )?.let { timelineMarkers.add(it) }
    }

    fun resolveDisplay(
        seekPos: Long?,
        isPaused: Boolean,
        liveFrame: AudioFrame?,
        smooth: Boolean = false,
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
        val dominant = if (level >= SignalLevel.GLOW) {
            AnalyzerProcessor.resolveDominantMetric(activeFrame)
        } else null

        // Headline display status is taken directly from the quantum window aggregated status in activeFrame
        val activeDisplayStatus = AnalyzerUiMapper.resolveContinuousStatus(activeFrame)

        return AnalyzerDisplaySnapshot(
            displayFrame = activeFrame,
            jitterLevel = smoothedJitter,
            pitchLevel = smoothedPitch,
            rmsLevel = smoothedRms,
            signalLevel = level,
            dominantMetric = dominant,
            activeInterpretation = activeDisplayStatus,
        )
    }
}
