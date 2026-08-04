package application.liedetector.uicore.widgets

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import application.liedetector.uicore.theme.tokens.ColorToken
import application.liedetector.uicore.types.BackgroundMode

@Stable
sealed class AppBackground {

    @Immutable
    data class Solid(
        val colorToken: ColorToken = ColorToken.BACKGROUND
    ) : AppBackground()

    @Immutable
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
