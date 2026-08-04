package application.liedetector.uicore.models

import androidx.compose.runtime.Stable
import application.liedetector.uicore.actions.WidgetAction

@Stable
data class MenuItem(
    val id: String,
    val iconKey: String? = null,
    val titleKey: String? = null,
    val action: WidgetAction
)
