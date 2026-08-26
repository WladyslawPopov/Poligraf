package application.poligraf.presentation.main.data

import androidx.compose.runtime.Stable
import application.poligraf.domain.model.AudioFrame
import application.poligraf.ui.foundation.types.AnalyzerSkin
import application.poligraf.ui.foundation.types.AnalyzerMarker
import application.poligraf.ui.theme.tokens.StringToken

@Stable
data class AnalyzerState(
    val currentFrame: AudioFrame? = null,
    val durationText: String = "00:00",
    val isRecording: Boolean = false,
    val isPaused: Boolean = false,
    val isAnomalous: Boolean = false,
    val currentSkin: AnalyzerSkin = AnalyzerSkin.STATE_MAP,
    val isReadOnly: Boolean = false,
    val isCalibrated: Boolean = false,
    
    // Normalized metrics for UI (0.0 to 1.0)
    val jitterLevel: Float = 0f,
    val pitchLevel: Float = 0f,
    val rmsLevel: Float = 0f,
    
    // Timeline and Seeking
    val timelineMarkers: List<AnalyzerMarker> = emptyList(),
    val currentDurationMillis: Long = 0,
    val seekPositionMillis: Long? = null,
    val activeInterpretation: StringToken? = null,
    val displayFrame: AudioFrame? = null,
    val isDisplayAnomalous: Boolean = false
)
