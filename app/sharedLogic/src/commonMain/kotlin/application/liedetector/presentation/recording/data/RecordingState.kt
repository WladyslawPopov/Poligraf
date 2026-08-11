package application.liedetector.presentation.recording.data

import androidx.compose.runtime.Stable
import application.liedetector.domain.model.Subject
import application.liedetector.uicore.models.LayoutConfig
import application.liedetector.uicore.state.ScaffoldUiState
import application.liedetector.uicore.widgets.AppBackground
import application.liedetector.uicore.widgets.UiWidget
import application.liedetector.uicore.widgets.VoiceRecorder

import application.liedetector.presentation.recordingHistory.VoiceRecorderUiState

@Stable
data class RecordingState(
    override val background: AppBackground = AppBackground.AnimatedScales(),
    override val toolbar: UiWidget.AppToolbar? = null,
    override val layoutConfig: LayoutConfig = LayoutConfig(isCentered = false),
    val subject: Subject = Subject(),
    val widgets: List<UiWidget> = emptyList(),
    val materials: List<MaterialTag> = emptyList()
) : ScaffoldUiState

@Stable
data class MaterialTag(
    val id: String,
    val title: String,
    val icon: String? = null
)
