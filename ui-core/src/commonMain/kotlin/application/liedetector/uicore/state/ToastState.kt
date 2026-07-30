package application.liedetector.uicore.state

import androidx.compose.runtime.Stable
import application.liedetector.uicore.theme.tokens.StringToken
import application.liedetector.uicore.types.ToastType
import kotlinx.serialization.Serializable

@Stable
@Serializable
data class ToastState(
    val messageToken: StringToken? = null,
    val messageRaw: String? = null,
    val type: ToastType
)
