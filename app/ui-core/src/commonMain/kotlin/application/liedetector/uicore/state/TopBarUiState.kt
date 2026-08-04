package application.liedetector.uicore.state

import androidx.compose.runtime.Stable
import application.liedetector.uicore.models.MenuItem
import application.liedetector.uicore.theme.tokens.StringToken

@Stable
data class TopBarUiState(
    val titleToken: StringToken? = null,
    val titleRaw: String? = null,
    val leadingIcon: String? = "menu",
    val menuItems: List<MenuItem> = emptyList(),
    val isTransparent: Boolean = true
)
