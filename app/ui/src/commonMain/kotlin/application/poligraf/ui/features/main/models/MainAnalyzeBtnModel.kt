package application.poligraf.ui.features.main.models

import androidx.compose.runtime.Immutable

@Immutable
data class MainAnalyzeBtnModel(
    val id: String = "analyze_btn",
    val isEnabled: Boolean = true,
)
