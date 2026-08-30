package application.poligraf.ui.foundation.models

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import application.poligraf.domain.model.MarkerShape
import application.poligraf.ui.theme.tokens.ColorToken

@Stable
data class AnalyzerMarker(
    val id: String,
    val timestampMillis: Long,
    val timestampText: String,
    val colorToken: ColorToken,
    val isAnomaly: Boolean = false,
    val shape: MarkerShape = MarkerShape.CIRCLE
)

@Immutable
data class SessionNoteUiModel(
    val id: String,
    val timestampMillis: Long,
    val timestampText: String,
    val text: String,
    val markerColor: ColorToken? = null,
    val markerShape: MarkerShape? = null
)
