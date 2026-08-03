package application.liedetector.uicore.widgets

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import application.liedetector.uicore.theme.tokens.ColorToken
import application.liedetector.uicore.theme.tokens.StringToken
import application.liedetector.uicore.theme.tokens.TypographyToken
import application.liedetector.uicore.types.WidgetAction
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Stable
@Serializable
@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator("type")
sealed class UiWidget {
    abstract val id: String

    @Immutable
    @Serializable
    @SerialName("app_toolbar")
    data class AppToolbar(
        override val id: String,
        val titleToken: StringToken? = null,
        val menuAction: WidgetAction = WidgetAction.OPEN_SETTINGS,
        val profileAction: WidgetAction = WidgetAction.OPEN_PROFILE,
        val backgroundColor: ColorToken = ColorToken.BACKGROUND,
        val contentColor: ColorToken = ColorToken.TEXT_PRIMARY,
        val typographyToken: TypographyToken = TypographyToken.HEADER
    ) : UiWidget()

    @Immutable
    @Serializable
    @SerialName("welcome_text")
    data class WelcomeText(
        override val id: String,
        val textToken: StringToken,
        val emoji: String? = null,
        val colorToken: ColorToken = ColorToken.TEXT_PRIMARY,
        val typographyToken: TypographyToken = TypographyToken.HEADER,
        val typingDelay: Long = 40L
    ) : UiWidget()

    @Immutable
    @Serializable
    data class SubjectCard(
        val id: String,
        val titleToken: StringToken = StringToken.SUBJECT_NEW_TITLE,
        val title: String? = null,
        val emoji: String,
        val action: WidgetAction,
        val backgroundColor: ColorToken = ColorToken.SURFACE_VARIANT,
        val titleColor: ColorToken = ColorToken.TEXT_PRIMARY,
        val titleTypography: TypographyToken = TypographyToken.SUBHEADER,
        val buttonColor: ColorToken = ColorToken.PRIMARY
    )

    @Immutable
    @Serializable
    @SerialName("subject_slider")
    data class SubjectSlider(
        override val id: String,
        val items: List<SubjectCard>,
        val itemSpacing: Int = 16
    ) : UiWidget()
}
