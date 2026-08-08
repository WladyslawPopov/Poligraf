package application.liedetector.uicore.widgets

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import application.liedetector.uicore.theme.tokens.ColorToken
import application.liedetector.uicore.theme.tokens.StringToken
import application.liedetector.uicore.theme.tokens.TypographyToken
import application.liedetector.uicore.actions.NavigationAction
import application.liedetector.uicore.actions.WidgetAction

@Stable
sealed class UiWidget {
    abstract val id: String

    @Immutable
    data class AppToolbar(
        override val id: String,
        val titleToken: StringToken? = null,
        val subtitleToken: StringToken? = null,
        val menuAction: WidgetAction = NavigationAction.Settings,
        val profileAction: WidgetAction = NavigationAction.Profile,
        val backgroundColor: ColorToken = ColorToken.BACKGROUND,
        val contentColor: ColorToken = ColorToken.TEXT_PRIMARY,
        val typographyToken: TypographyToken = TypographyToken.HEADER
    ) : UiWidget()

    @Immutable
    data class WelcomeText(
        override val id: String,
        val textToken: StringToken,
        val emoji: String? = null,
        val colorToken: ColorToken = ColorToken.TEXT_PRIMARY,
        val typographyToken: TypographyToken = TypographyToken.HEADER,
        val typingDelay: Long = 40L
    ) : UiWidget()

    @Immutable
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
    data class SubjectSlider(
        override val id: String,
        val items: List<SubjectCard>,
        val itemSpacing: Int = 16,
        val displayMode: DisplayMode = DisplayMode.FULL
    ) : UiWidget() {
        enum class DisplayMode { FULL, RECT_STORY }
    }

    @Immutable
    data class SubjectList(
        override val id: String,
        val items: List<SubjectCard>,
        val isSelectionMode: Boolean = false,
        val selectedIds: Set<String> = emptySet()
    ) : UiWidget()

    @Immutable
    data class VoiceRecorder(
        override val id: String,
        val status: Status = Status.IDLE,
        val durationMillis: Long = 0,
        val amplitudes: List<Float> = emptyList(),
        val filePath: String? = null
    ) : UiWidget() {
        enum class Status { IDLE, RECORDING, PAUSED, FINISHED }
    }
}
