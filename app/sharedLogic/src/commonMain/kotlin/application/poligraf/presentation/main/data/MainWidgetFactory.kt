package application.poligraf.presentation.main.data

import application.poligraf.domain.model.Subject
import application.poligraf.uicore.actions.RecordingAction
import application.poligraf.uicore.theme.tokens.ColorToken
import application.poligraf.uicore.theme.tokens.TypographyToken
import application.poligraf.uicore.widgets.UiWidget

object MainWidgetFactory {

    fun createSubjectSlider(
        displayMode: UiWidget.SubjectSlider.DisplayMode
    ): UiWidget.SubjectSlider {
        val defaultCard = UiWidget.SubjectCard(
            id = "new_recording",
            titleProvider = { it.subjects.newButton },
            emoji = "🕵️",
            action = RecordingAction.StartNew,
            backgroundColor = ColorToken.GLASS_BASE,
            titleColor = ColorToken.TEXT_PRIMARY,
            titleTypography = TypographyToken.SUBHEADER,
            buttonColor = ColorToken.ACCENT_PRIMARY
        )

        return UiWidget.SubjectSlider(
            id = "main_slider",
            itemSpacing = 16,
            items = listOf(defaultCard), // Only templates here
            displayMode = displayMode
        )
    }

    fun createSubjectList(subjects: List<Subject>): UiWidget.SubjectList {
        val items = subjects.map { data ->
            UiWidget.SubjectCard(
                id = data.id,
                title = data.name,
                emoji = data.avatar,
                action = RecordingAction.Open(data.id),
                backgroundColor = ColorToken.GLASS_BASE
            )
        }
        return UiWidget.SubjectList(
            id = "main_subjects_list",
            items = items
        )
    }
}
