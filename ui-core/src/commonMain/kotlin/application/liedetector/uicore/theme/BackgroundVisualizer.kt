package application.liedetector.uicore.theme

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.StateFlow

/**
 * State for the hypnotic scales background.
 */
@Stable
data class BackgroundState(
    val tiltX: Float = 0f,
    val tiltY: Float = 0f,
    val intensity: Float = 0.5f,
    val colorToken: ColorToken = ColorToken.BACKGROUND
)

/**
 * Interface for platforms to provide sensor-driven background updates.
 */
interface BackgroundVisualizer {
    val state: StateFlow<BackgroundState>
    
    fun setIntensity(value: Float)
    fun onTap(x: Float, y: Float)
}
