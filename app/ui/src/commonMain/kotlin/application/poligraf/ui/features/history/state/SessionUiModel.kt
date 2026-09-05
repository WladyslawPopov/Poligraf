package application.poligraf.ui.features.history.state

import androidx.compose.runtime.Immutable

@Immutable
data class SessionUiModel(
    val id: String,
    val title: String,
    val dateText: String,
    val durationMillis: Long,
    val fullAnomalyCount: Int,
    val halftoneAnomalyCount: Int,
    val noteCount: Int,
    val timestamp: Long,
) {
    val markerCount: Int get() = fullAnomalyCount + halftoneAnomalyCount
}
