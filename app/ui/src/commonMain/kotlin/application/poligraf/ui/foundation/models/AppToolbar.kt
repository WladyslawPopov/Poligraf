package application.poligraf.ui.foundation.models

import androidx.compose.runtime.Immutable
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.IconToken
import application.poligraf.ui.theme.tokens.StringToken

/**
 * Common model for top bars and toolbars across the app.
 */
@Immutable
data class AppToolbar(
    val titleToken: StringToken = StringToken.APP_NAME,
    val navigationIcon: IconToken? = IconToken.ARROW_BACK,
    val navigationAction: Any? = null,
    val trailingActions: List<ToolbarAction> = emptyList(),
    val backgroundColor: ColorToken = ColorToken.SURFACE_BACKGROUND,
    val contentColor: ColorToken = ColorToken.TEXT_PRIMARY,
)
