package application.liedetector.uiwidgets.states

import androidx.compose.runtime.Stable
import application.liedetector.uiwidgets.models.MenuItemDto

@Stable
data class TopBarUiState(
    val title: String = "",
    val leadingIcon: String? = "menu",
    val menuItems: List<MenuItemDto> = emptyList(),
    val isTransparent: Boolean = true
)
