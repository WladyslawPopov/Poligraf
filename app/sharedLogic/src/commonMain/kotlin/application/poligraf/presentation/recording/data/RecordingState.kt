package application.poligraf.presentation.recording.data

import androidx.compose.runtime.Stable
import application.poligraf.domain.model.Subject
import application.poligraf.uicore.models.LayoutConfig
import application.poligraf.uicore.state.ScaffoldUiState
import application.poligraf.uicore.widgets.AppBackground
import application.poligraf.uicore.widgets.UiWidget
import application.poligraf.uicore.widgets.VoiceRecorder

import application.poligraf.uicore.state.VoiceRecorderUiState

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
