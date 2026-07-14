package application.liedetector.uiwidgets.models

import application.liedetector.uicore.theme.ColorToken
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class WidgetDto {
    abstract val id: String

    @Serializable
    @SerialName("header")
    data class Header(
        override val id: String,
        val titleKey: String, // String resource key
        val subtitleKey: String? = null
    ) : WidgetDto()

    @Serializable
    @SerialName("verdict_card")
    data class VerdictCard(
        override val id: String,
        val verdictKey: String,
        val score: Int,
        val colorToken: ColorToken
    ) : WidgetDto()

    @Serializable
    @SerialName("microphone_button")
    data class MicrophoneButton(
        override val id: String,
        val action: WidgetAction
    ) : WidgetDto()

    @Serializable
    @SerialName("acoustic_graph")
    data class AcousticGraph(
        override val id: String,
        val points: List<Float>,
        val colorToken: ColorToken = ColorToken.PRIMARY
    ) : WidgetDto()

    @Serializable
    @SerialName("standard_button")
    data class StandardButton(
        override val id: String,
        val textKey: String,
        val action: WidgetAction,
        val isPrimary: Boolean = true
    ) : WidgetDto()
}

@Serializable
enum class WidgetAction {
    START_RECORDING,
    STOP_RECORDING,
    OPEN_HISTORY,
    OPEN_SETTINGS,
    RETRY_ANALYSIS
}
