package application.liedetector.uiwidgets.states

import androidx.compose.runtime.Stable
import application.liedetector.uiwidgets.models.MenuItemDto

import application.liedetector.uicore.theme.StringToken

@Stable
data class TopBarUiState(
    val titleToken: StringToken? = null,
    val titleRaw: String? = null,
    val leadingIcon: String? = "menu",
    val menuItems: List<MenuItemDto> = emptyList(),
    val isTransparent: Boolean = true
)
