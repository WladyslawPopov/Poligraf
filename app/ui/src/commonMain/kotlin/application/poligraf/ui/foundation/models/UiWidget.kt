package application.poligraf.ui.foundation.models

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.StringToken
import application.poligraf.ui.foundation.actions.WidgetAction
import application.poligraf.ui.foundation.state.BackgroundMode

@Stable
sealed class UiWidget {
    abstract val id: String

    @Immutable
    data class WelcomeText(
        override val id: String,
        val textToken: StringToken = StringToken.WELCOME_1,
        val emoji: String? = null,
        val colorToken: ColorToken = ColorToken.TEXT_PRIMARY,
        val typingDelay: Long = 40L
    ) : UiWidget()

    @Immutable
    data class AnalyzeBtn(
        override val id: String,
        val action: WidgetAction
    ) : UiWidget()

    @Immutable
    data class Analyzer(
        override val id: String,
        val mode: BackgroundMode = BackgroundMode.RECORDING
    ) : UiWidget()
}
