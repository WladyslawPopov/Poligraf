package application.poligraf.ui.foundation.models

import androidx.compose.runtime.Immutable
import application.poligraf.ui.foundation.actions.WidgetAction
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.IconToken

@Immutable
data class ToolbarAction(
    val icon: IconToken,
    val action: WidgetAction,
    val tint: ColorToken? = null
)
