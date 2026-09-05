package application.poligraf.data.analyzer.dsp

import application.poligraf.domain.analyzer.model.AnomalyMarker
import application.poligraf.domain.analyzer.model.AudioFrame
import application.poligraf.domain.analyzer.model.QuantumAnalysis
import application.poligraf.domain.analyzer.types.AnalysisStatus
import application.poligraf.domain.analyzer.types.DominantMetric
import application.poligraf.domain.analyzer.types.SensitivityLevel

/**
 * Dedicated DSP component in Data layer responsible for aggregating Quantum Window subframes
 * into clean domain [QuantumAnalysis] models and extracting Engine-level [AnomalyMarker]s.
 */
object QuantumWindowAggregator {

    fun aggregateWindow(
        frames: List<AudioFrame>,
        sensitivity: SensitivityLevel = SensitivityLevel.MEDIUM,
    ): QuantumAnalysis {
        if (frames.isEmpty()) {
            return QuantumAnalysis(
                primaryStatus = AnalysisStatus.CALM,
                primaryAlpha = 1.0f,
                secondaryStatusesWithScores = emptyList()
            )
        }

        // 1. Warmup / Calibration phase (timestamp < 5000ms)
        val maxTimestamp = frames.maxOf { it.timestamp }
        if (maxTimestamp < AnalyzerThresholds.WARMUP_DURATION_MS) {
            return QuantumAnalysis(
                primaryStatus = AnalysisStatus.WARMUP,
                primaryAlpha = 1.0f,
                secondaryStatusesWithScores = emptyList()
            )
        }

        // 2. Filter active speech subframes in this quantum window
        val voiceFrames = frames.filter {
            it.stressScore > 0f || it.jitterScore > 0f || it.pitchScore > 0f || it.rmsScore > 0f
        }

        // Must have speech for at least 15% of window
        val minVoiceFrames = (frames.size * 0.15f).coerceAtLeast(1f)
        if (voiceFrames.size < minVoiceFrames) {
            return QuantumAnalysis(
                primaryStatus = AnalysisStatus.CALM,
                primaryAlpha = 1.0f,
                secondaryStatusesWithScores = emptyList()
            )
        }

        // 3. Resolve status & score for each voice frame
        val statusScorePairs = voiceFrames
            .asSequence()
            .map { frame ->
                val resolvedStatus = AnalysisStatusResolver.resolve(
                    rms = frame.rmsScore,
                    jitterScore = frame.jitterScore,
                    pitchScore = frame.pitchScore,
                    rmsScore = frame.rmsScore,
                    timestamp = frame.timestamp,
                    sensitivity = sensitivity
                )
                val maxBiomarker =
                    maxOf(frame.stressScore, frame.jitterScore, frame.pitchScore, frame.rmsScore)
                val score = maxBiomarker.coerceIn(0.15f, 1.0f)
                resolvedStatus to score
            }
            .filter { (status, _) ->
                status != AnalysisStatus.CALM && status != AnalysisStatus.WARMUP
            }
            .groupBy { it.first }
            .map { (status, pairs) ->
                status to pairs.maxOf { it.second }
            }
            .sortedWith(
                compareByDescending<Pair<AnalysisStatus, Float>> { (it.second * 10f).toInt() }
                    .thenBy { it.first.ordinal }
            )
            .toList()

        if (statusScorePairs.isEmpty()) {
            return QuantumAnalysis(
                primaryStatus = AnalysisStatus.CALM,
                primaryAlpha = 1.0f,
                secondaryStatusesWithScores = emptyList()
            )
        }

        // 4. Check if a Full Anomaly broke threshold
        val fullAnomalyPair = statusScorePairs.firstOrNull {
            AnalysisStatusResolver.isFullAnomaly(it.first)
        }

        return if (fullAnomalyPair != null) {
            val primaryStatus = fullAnomalyPair.first
            val primaryAlpha = fullAnomalyPair.second

            val secondaries = statusScorePairs
                .filter { it.first != primaryStatus }
                .take(2)

            QuantumAnalysis(
                primaryStatus = primaryStatus,
                primaryAlpha = primaryAlpha,
                secondaryStatusesWithScores = secondaries
            )
        } else {
            val primaryStatus = AnalysisStatus.CALM
            val primaryAlpha = 1.0f

            val secondaries = statusScorePairs.take(2)

            QuantumAnalysis(
                primaryStatus = primaryStatus,
                primaryAlpha = primaryAlpha,
                secondaryStatusesWithScores = secondaries
            )
        }
    }

    fun extractWindowMarkers(
        frames: List<AudioFrame>,
        quantumWindowMs: Long = 2500L,
        sensitivity: SensitivityLevel = SensitivityLevel.MEDIUM,
    ): List<AnomalyMarker> {
        if (frames.isEmpty()) return emptyList()

        val markers = mutableListOf<AnomalyMarker>()
        var lastMarkerTime = -10000L
        val clusterWindow = 800L

        val maxTimestamp = frames.last().timestamp
        val stepMs = quantumWindowMs.coerceAtLeast(1000L) // Non-overlapping quantum windows

        var windowStart = 0L
        while (windowStart <= maxTimestamp) {
            val windowEnd = windowStart + quantumWindowMs
            val windowFrames = frames.filter { it.timestamp in windowStart until windowEnd }

            if (windowFrames.isNotEmpty()) {
                val maxTimeInWindow = windowFrames.maxOf { it.timestamp }
                if (maxTimeInWindow >= 5000L) { // Exclude warmup calibration phase (0..5000ms)
                    val quantumAnalysis = aggregateWindow(windowFrames, sensitivity)

                    val activeStatuses = mutableListOf<Pair<AnalysisStatus, Float>>()
                    if (quantumAnalysis.primaryStatus != AnalysisStatus.CALM &&
                        quantumAnalysis.primaryStatus != AnalysisStatus.WARMUP
                    ) {
                        activeStatuses.add(quantumAnalysis.primaryStatus to quantumAnalysis.primaryAlpha)
                    }
                    activeStatuses.addAll(quantumAnalysis.secondaryStatusesWithScores)

                    for ((status, score) in activeStatuses) {
                        val statusDominant = when (status) {
                            AnalysisStatus.STRESS_SINGLE,
                            AnalysisStatus.PITCH_DROP,
                            AnalysisStatus.PANIC,
                            AnalysisStatus.CONFRONTATION,
                                -> DominantMetric.PITCH

                            AnalysisStatus.FEAR_SINGLE,
                            AnalysisStatus.SUBDUED_TREMOR,
                            AnalysisStatus.DISORGANIZATION,
                                -> DominantMetric.JITTER

                            AnalysisStatus.PRESSURE_SINGLE,
                            AnalysisStatus.RMS_DROP,
                            AnalysisStatus.AGGRESSION,
                                -> DominantMetric.RMS

                            else -> null
                        }

                        val peakFrame = windowFrames.maxByOrNull { frame ->
                            when (statusDominant) {
                                DominantMetric.PITCH -> frame.pitchScore
                                DominantMetric.JITTER -> frame.jitterScore
                                DominantMetric.RMS -> frame.rmsScore
                                else -> maxOf(frame.jitterScore, frame.pitchScore, frame.rmsScore)
                            }
                        } ?: windowFrames.last()

                        val effectiveDominant = statusDominant
                            ?: peakFrame.dominantMetric
                            ?: when {
                                peakFrame.jitterScore >= peakFrame.pitchScore && peakFrame.jitterScore >= peakFrame.rmsScore * 0.75f && peakFrame.jitterScore > 0f -> DominantMetric.JITTER
                                peakFrame.pitchScore >= peakFrame.jitterScore && peakFrame.pitchScore >= peakFrame.rmsScore * 0.75f && peakFrame.pitchScore > 0f -> DominantMetric.PITCH
                                else -> DominantMetric.RMS
                            }

                        val isFull = AnalysisStatusResolver.isFullAnomaly(status)
                        val effectiveAlpha = if (isFull) {
                            (score * 0.40f + 0.60f).coerceIn(0.70f, 1.0f)
                        } else {
                            (score * 0.35f + 0.35f).coerceIn(0.40f, 0.70f)
                        }

                        val deterministicId = "m_window_${windowStart / stepMs}_${status.name}"

                        if ((peakFrame.timestamp - lastMarkerTime) >= clusterWindow) {
                            markers.add(
                                AnomalyMarker(
                                    id = deterministicId,
                                    timestampMillis = peakFrame.timestamp,
                                    status = status,
                                    dominantMetric = effectiveDominant,
                                    isFullAnomaly = isFull,
                                    alpha = effectiveAlpha
                                )
                            )
                            lastMarkerTime = peakFrame.timestamp
                        }
                    }
                }
            }

            windowStart += stepMs
        }

        return markers
    }
}
