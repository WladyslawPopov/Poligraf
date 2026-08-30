package application.poligraf.ui.foundation.models

import androidx.compose.runtime.Immutable

@Immutable
data class MainAnalyzeBtnModel(
    val id: String = "analyze_btn",
    val isEnabled: Boolean = true
)
