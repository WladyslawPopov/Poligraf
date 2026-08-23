package application.poligraf.ui.foundation.models

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.foundation.state.BackgroundMode

@Stable
sealed class AppBackground {

    @Immutable
    data class Solid(
        val colorToken: ColorToken = ColorToken.SURFACE_BACKGROUND
    ) : AppBackground()

    @Immutable
    data class AnimatedScales(
        val baseColor: ColorToken = ColorToken.SURFACE_BACKGROUND,
        val energyColor: ColorToken = ColorToken.ACCENT_ENERGY,
        val particleColor: ColorToken = ColorToken.SURFACE_VARIANT,
        val blurRadius: Float = 2.0f,
        val animationSpeed: Float = 1.0f,
        val mode: BackgroundMode = BackgroundMode.IDLE
    ) : AppBackground()
}
