package application.poligraf.ui.foundation.models

import androidx.compose.runtime.Stable

@Stable
data class MenuItem(
    val id: String,
    val iconKey: String? = null,
    val titleKey: String? = null,
    val action: Any
)
