package application.liedetector.uiwidgets.models

import kotlinx.serialization.Serializable

@Serializable
data class ScreenTemplate(
    val titleKey: String? = null,
    val leadingIconKey: String? = null,
    val menuItems: List<MenuItemDto> = emptyList(),
    val widgets: List<UiWidget> = emptyList()
)

@Serializable
data class MenuItemDto(
    val id: String,
    val iconKey: String? = null,
    val titleKey: String? = null,
    val action: WidgetAction
)
