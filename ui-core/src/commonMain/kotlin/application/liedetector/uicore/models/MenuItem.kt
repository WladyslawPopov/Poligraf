package application.liedetector.uicore.models

import application.liedetector.uicore.types.WidgetAction
import kotlinx.serialization.Serializable


@Serializable
data class MenuItem(
    val id: String,
    val iconKey: String? = null,
    val titleKey: String? = null,
    val action: WidgetAction
)
