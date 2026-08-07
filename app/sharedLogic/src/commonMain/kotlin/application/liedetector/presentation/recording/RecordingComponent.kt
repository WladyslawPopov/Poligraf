package application.liedetector.presentation.recording

import androidx.compose.runtime.Stable
import application.liedetector.component.ComponentContext

@Stable
class RecordingComponent(
    val context: ComponentContext,
    val viewModel: RecordingViewModel
) {
    fun goBack() {
        viewModel.goBack()
    }

    fun deleteRecording() {
        viewModel.deleteRecording()
    }
}
