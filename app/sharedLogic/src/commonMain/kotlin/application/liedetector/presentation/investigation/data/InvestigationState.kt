package application.liedetector.presentation.investigation.data

import androidx.compose.runtime.Stable
import application.liedetector.models.SubjectDto
import application.liedetector.uicore.models.LayoutConfig
import application.liedetector.uicore.state.ScaffoldUiState
import application.liedetector.uicore.widgets.AppBackground
import application.liedetector.uicore.widgets.UiWidget

@Stable
data class InvestigationState(
    override val background: AppBackground = AppBackground.AnimatedScales(),
    override val toolbar: UiWidget.AppToolbar? = null,
    override val layoutConfig: LayoutConfig = LayoutConfig(isCentered = false),
    val subject: SubjectDto? = null,
    val widgets: List<UiWidget> = emptyList()
) : ScaffoldUiState
