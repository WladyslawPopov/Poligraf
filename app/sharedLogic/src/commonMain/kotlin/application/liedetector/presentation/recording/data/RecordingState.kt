package application.liedetector.presentation.recording.data

import androidx.compose.runtime.Stable
import application.liedetector.domain.model.Subject
import application.liedetector.uicore.models.LayoutConfig
import application.liedetector.uicore.state.ScaffoldUiState
import application.liedetector.uicore.widgets.AppBackground
import application.liedetector.uicore.widgets.UiWidget

@Stable
data class RecordingState(
    override val background: AppBackground = AppBackground.AnimatedScales(),
    override val toolbar: UiWidget.AppToolbar? = null,
    override val layoutConfig: LayoutConfig = LayoutConfig(isCentered = false),
    val subject: Subject? = null,
    val widgets: List<UiWidget> = emptyList()
) : ScaffoldUiState
