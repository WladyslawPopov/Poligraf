package application.poligraf.ui.foundation.state

import androidx.compose.runtime.Stable
import application.poligraf.ui.foundation.models.MenuItem
import application.poligraf.ui.theme.AppStrings

@Stable
data class TopBarUiState(
    val titleProvider: ((AppStrings) -> String)? = null,
    val titleRaw: String? = null,
    val leadingIcon: String? = "menu",
    val menuItems: List<MenuItem> = emptyList(),
    val isTransparent: Boolean = true
)
