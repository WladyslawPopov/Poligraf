package application.liedetector.uicore.widgets

import androidx.compose.runtime.Stable
import application.liedetector.uicore.theme.tokens.ColorToken
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Stable
@Serializable
sealed class AppBackground {

    @Stable
    @Serializable
    @SerialName("solid")
    data class Solid(
        val colorToken: ColorToken = ColorToken.BACKGROUND
    ) : AppBackground()

    @Stable
    @Serializable
    @SerialName("animated_scales")
    data class AnimatedScales(
        val baseColor: ColorToken = ColorToken.BACKGROUND,
        val energyColor: ColorToken = ColorToken.ACCENT_ENERGY,
        val particleColor: ColorToken = ColorToken.SURFACE_VARIANT,
        val parallaxIntensity: Float = 1.0f,
        val blurRadius: Float = 2.0f,
        val animationSpeed: Float = 1.0f,
        val mode: BackgroundMode = BackgroundMode.IDLE
    ) : AppBackground()
}

@Serializable
enum class BackgroundMode {
    IDLE,
    PROCESSING,
    RECORDING,
    ERROR,
    SUCCESS
}
