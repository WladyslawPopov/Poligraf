package application.poligraf.ui.features.analyzer.models

import androidx.compose.runtime.Immutable
import application.poligraf.domain.analyzer.types.MarkerShape
import application.poligraf.ui.theme.tokens.ColorToken

@Immutable
data class SessionNoteUiModel(
    val id: String,
    val timestampMillis: Long,
    val timestampText: String,
    val text: String,
    val markerColor: ColorToken? = null,
    val markerShape: MarkerShape? = null,
)
