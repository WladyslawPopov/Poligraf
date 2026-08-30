package application.poligraf.ui.foundation.models

import androidx.compose.runtime.Immutable
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.StringToken

@Immutable
data class MainWelcomeModel(
    val id: String = "main_welcome",
    val textToken: StringToken = StringToken.WELCOME_1,
    val emoji: String? = null,
    val colorToken: ColorToken = ColorToken.TEXT_PRIMARY,
    val typingDelay: Long = 40L
)
