package application.poligraf.presentation.analyzer.logic

import application.poligraf.domain.model.AudioFrame
import application.poligraf.engine.dsp.DominantMetric
import application.poligraf.engine.dsp.SignalLevel
import application.poligraf.ui.theme.tokens.StringToken

/**
 * Fully resolved display state for one analysis frame. ViewModels copy this into
 * [application.poligraf.ui.foundation.state.AnalyzerState] without re-deriving anything.
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
