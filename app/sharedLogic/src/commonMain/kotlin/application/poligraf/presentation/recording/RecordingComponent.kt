package application.poligraf.presentation.recording

import androidx.compose.runtime.Stable
import application.poligraf.engine.component.ComponentContext

import application.poligraf.uicore.state.VoiceRecorderAction

@Stable
class RecordingComponent(
    val context: ComponentContext,
    val viewModel: RecordingViewModel
) {
    fun goBack() {
        viewModel.goBack()
    }

    fun handleVoiceAction(action: VoiceRecorderAction) {
        // ViewModel is stripped of recording logic, navigation only
        if (action is VoiceRecorderAction.ToggleRecord) {
            viewModel.onMicClicked()
        }
    }

    fun onMaterialTagClicked(tagId: String) {
        viewModel.onMaterialTagClicked(tagId)
    }
}
