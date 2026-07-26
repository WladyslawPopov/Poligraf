package application.liedetector.uicore.widgets

import androidx.compose.runtime.Immutable
import application.liedetector.uicore.theme.ColorToken
import application.liedetector.uicore.theme.StringToken
import application.liedetector.uicore.types.WidgetAction
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
sealed class UiWidget {
    abstract val id: String

    @Immutable
    @Serializable
    @SerialName("header")
    data class Header(
        override val id: String,
        val titleToken: StringToken,
        val subtitleToken: StringToken? = null
    ) : UiWidget()

    @Immutable
    @Serializable
    @SerialName("verdict_card")
    data class VerdictCard(
        override val id: String,
        val verdictToken: StringToken,
        val score: Int,
        val colorToken: ColorToken
    ) : UiWidget()

    @Immutable
    @Serializable
    @SerialName("microphone_button")
    data class MicrophoneButton(
        override val id: String,
        val action: WidgetAction
    ) : UiWidget()

    @Immutable
    @Serializable
    @SerialName("acoustic_graph")
    data class AcousticGraph(
        override val id: String,
        val points: List<Float>,
        val colorToken: ColorToken = ColorToken.PRIMARY
    ) : UiWidget()

    @Immutable
    @Serializable
    @SerialName("standard_button")
    data class StandardButton(
        override val id: String,
        val textToken: StringToken,
        val action: WidgetAction,
        val isPrimary: Boolean = true
    ) : UiWidget()
}
