package application.poligraf.ui.features.analyzer.models

import androidx.compose.runtime.Stable
import application.poligraf.domain.analyzer.types.MarkerShape
import application.poligraf.ui.theme.tokens.ColorToken

@Stable
data class AnalyzerMarker(
    val id: String,
    val timestampMillis: Long,
    val timestampText: String,
    val colorToken: ColorToken,
    val isAnomaly: Boolean = false,
    val shape: MarkerShape = MarkerShape.CIRCLE,
)
