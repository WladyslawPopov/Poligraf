package application.poligraf.ui.features.analyzer.visualizations

import androidx.compose.runtime.Composable
import application.poligraf.domain.model.AnalyzerSkin

@Composable
fun VisualizationContent(
    skin: AnalyzerSkin,
    jitter: Float,
    pitch: Float,
    rms: Float,
    isPaused: Boolean
) {
    when (skin) {
        AnalyzerSkin.STATE_MAP -> StateMapVisualization(
            jitterLevel = jitter,
            pitchLevel = pitch,
            rmsLevel = rms
        )

        AnalyzerSkin.VOICE_RIBBON -> VoiceRibbonVisualization(
            jitterLevel = jitter,
            pitchLevel = pitch,
            rmsLevel = rms,
            isPaused = isPaused
        )

        AnalyzerSkin.EQUALIZER -> EqualizerVisualization(
            jitterLevel = jitter,
            pitchLevel = pitch,
            rmsLevel = rms
        )

        AnalyzerSkin.RINGS -> RingsVisualization(
            jitterLevel = jitter,
            pitchLevel = pitch,
            rmsLevel = rms
        )
    }
}
