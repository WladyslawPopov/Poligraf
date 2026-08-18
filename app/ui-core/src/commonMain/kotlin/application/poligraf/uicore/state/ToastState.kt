package application.poligraf.uicore.state

import androidx.compose.runtime.Stable
import application.poligraf.uicore.theme.tokens.StringToken
import application.poligraf.uicore.types.ToastType

@Stable
data class ToastState(
    val messageToken: StringToken? = null,
    val messageRaw: String? = null,
    val type: ToastType
)
