package application.liedetector.uicore.models

import kotlinx.serialization.Serializable

@Serializable
data class DisplayMetrics(
    val isLandscape: Boolean = false,
    val windowWidthPx: Int = 0,
    val windowHeightPx: Int = 0
)
