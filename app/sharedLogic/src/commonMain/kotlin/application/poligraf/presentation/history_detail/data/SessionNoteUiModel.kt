package application.poligraf.presentation.history_detail.data

import androidx.compose.runtime.Immutable
import application.poligraf.domain.model.MarkerShape
import application.poligraf.ui.theme.tokens.ColorToken

@Immutable
data class SessionNoteUiModel(
    val id: String,
    val timestampMillis: Long,
    val timestampText: String,
    val text: String,
    val markerColor: ColorToken? = null,
    val markerShape: MarkerShape? = null
)
