package application.liedetector.uicore.widgets

import application.liedetector.uicore.models.MenuItem
import kotlinx.serialization.Serializable

@Serializable
data class ScreenTemplate(
    val titleKey: String? = null,
    val leadingIconKey: String? = null,
    val menuItems: List<MenuItem> = emptyList(),
    val widgets: List<UiWidget> = emptyList()
)

