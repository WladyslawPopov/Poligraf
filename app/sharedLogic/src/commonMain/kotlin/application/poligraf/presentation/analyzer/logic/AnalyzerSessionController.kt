package application.poligraf.presentation.analyzer.logic

import application.poligraf.domain.model.AudioFrame
import application.poligraf.domain.model.MarkerShape
import application.poligraf.engine.config.AnalyzerThresholds
import application.poligraf.engine.dsp.SignalLevel
import application.poligraf.engine.utils.nowAsEpochMilliseconds
import application.poligraf.ui.foundation.models.AnalyzerMarker
import application.poligraf.ui.theme.tokens.StringToken

/**
 * Pure, reusable holder for the analyzer "brain".
 *
 * Both [AnalyzerViewModel] (LIVE) and [HistoryDetailViewModel] (REVIEW) delegate the
 * exact same frame-history, marker and display-resolution logic here.
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

    /**
     * Updates the shape used for future markers and applies it to existing ones.
     */
    fun setMarkerShape(shape: MarkerShape): List<AnalyzerMarker> {
        markerShape = shape
        val updated = timelineMarkers.map { it.copy(shape = shape) }
        timelineMarkers.clear()
        timelineMarkers.addAll(updated)
        return timelineMarkers.toList()
    }

    /**
     * Ingests one live frame, appending history and clustering anomaly markers.
     */
    fun onLiveFrame(frame: AudioFrame) {
        val lastTimestamp = frameHistory.lastOrNull()?.timestamp ?: -1L
        if (frame.timestamp > lastTimestamp) {
            frameHistory.add(frame)
        }
        appendMarkerIfNeeded(frame)
    }

    /**
     * Replaces history from persisted frames (resume draft or history review).
     */
    fun loadFrames(frames: List<AudioFrame>) {
        frameHistory.clear()
        frameHistory.addAll(frames)
        timelineMarkers.clear()
        frames.forEach { appendMarkerIfNeeded(it) }
    }

    private fun appendMarkerIfNeeded(frame: AudioFrame) {
        val level = AnalyzerProcessor.resolveSignalLevel(frame)
        // Set dominant metric for all visible levels (Glow and above) to have colored markers
        val dominant = if (level != SignalLevel.NONE) {
            AnalyzerProcessor.resolveDominantMetric(frame)
        } else null
        val lastMarkerTime = timelineMarkers.lastOrNull()?.timestampMillis ?: -10000L
        AnalyzerProcessor.createAnomalyMarker(
            frame = frame,
            shape = markerShape,
            lastMarkerTimestamp = lastMarkerTime,
            signalLevel = level,
            dominantMetric = dominant
        )?.let { timelineMarkers.add(it) }
    }

    /**
     * Resolves the full display snapshot for the current seek/frame.
     * Note: AudioFrames from the repository are already smoothed at 20Hz.
     */
    fun resolveDisplay(
        seekPos: Long?,
        isPaused: Boolean,
        liveFrame: AudioFrame?,
        smooth: Boolean = false, // Disabled by default as frames are pre-smoothed
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

        val currentInterpretation = AnalyzerProcessor.determineInterpretation(
            smoothedJitter, smoothedPitch, smoothedRms
        )
        val now = nowAsEpochMilliseconds()

        val finalInterpretation = if (isPaused) {
            currentInterpretation
        } else {
            when {
                currentInterpretation != null -> {
                    lastInterpretation = currentInterpretation
                    interpretationTimestamp = now
                    currentInterpretation
                }

                lastInterpretation != null &&
                        (now - interpretationTimestamp) < AnalyzerThresholds.INTERPRETATION_STICKY_MS -> lastInterpretation

                else -> {
                    lastInterpretation = null
                    null
                }
            }
        }


        return AnalyzerDisplaySnapshot(
            displayFrame = activeFrame,
            jitterLevel = smoothedJitter,
            pitchLevel = smoothedPitch,
            rmsLevel = smoothedRms,
            signalLevel = level,
            dominantMetric = dominant,
            activeInterpretation = finalInterpretation,
        )
    }
}
