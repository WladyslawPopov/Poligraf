package application.liedetector.presentation.recording

import androidx.compose.runtime.Stable
import application.liedetector.engine.component.ComponentContext

import application.liedetector.presentation.recordingHistory.VoiceRecorderAction

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
