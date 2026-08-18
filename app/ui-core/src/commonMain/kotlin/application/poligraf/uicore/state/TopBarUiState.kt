package application.poligraf.uicore.state

import androidx.compose.runtime.Stable
import application.poligraf.uicore.models.MenuItem
import application.poligraf.uicore.theme.AppStrings

@Stable
data class TopBarUiState(
    val titleProvider: ((AppStrings) -> String)? = null,
    val titleRaw: String? = null,
    val leadingIcon: String? = "menu",
    val menuItems: List<MenuItem> = emptyList(),
    val isTransparent: Boolean = true
)
