package application.poligraf.ui.foundation.state

import androidx.compose.runtime.Stable
import application.poligraf.ui.theme.AppStrings
import application.poligraf.ui.foundation.state.ToastType

@Stable
data class ToastState(
    val provider: ((AppStrings) -> String)? = null,
    val messageRaw: String? = null,
    val type: ToastType
)
