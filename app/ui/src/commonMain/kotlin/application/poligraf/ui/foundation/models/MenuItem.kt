package application.poligraf.ui.foundation.models

import androidx.compose.runtime.Stable
import application.poligraf.ui.foundation.actions.WidgetAction

@Stable
data class MenuItem(
    val id: String,
    val iconKey: String? = null,
    val titleKey: String? = null,
    val action: WidgetAction
)
