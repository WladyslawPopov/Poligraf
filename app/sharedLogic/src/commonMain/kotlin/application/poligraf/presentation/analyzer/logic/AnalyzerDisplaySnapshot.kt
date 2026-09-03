package application.poligraf.presentation.analyzer.logic

import application.poligraf.domain.analyzer.model.AudioFrame
import application.poligraf.domain.analyzer.types.DominantMetric
import application.poligraf.domain.analyzer.types.SignalLevel
import application.poligraf.ui.theme.tokens.StringToken

/**
 * Fully resolved display state for one analysis frame. ViewModels copy this into
 * [application.poligraf.ui.features.analyzer.state.AnalyzerState] without re-deriving anything.
 */
data class AnalyzerDisplaySnapshot(
    val displayFrame: AudioFrame?,
    val jitterLevel: Float,
    val pitchLevel: Float,
    val rmsLevel: Float,
    val signalLevel: SignalLevel,
    val dominantMetric: DominantMetric?,
    val activeInterpretation: StringToken?,
)
