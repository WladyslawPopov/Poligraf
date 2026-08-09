package application.liedetector.presentation.recordingHistory

import androidx.compose.runtime.Stable
import application.liedetector.engine.component.ComponentContext
import application.liedetector.uicore.widgets.UiWidget

@Stable
class RecordingsHistoryComponent(
    val context: ComponentContext,
    val viewModel: RecordingsHistoryViewModel
) {
    fun goBack() {
        viewModel.goBack()
    }

    fun deleteRecording() {
        viewModel.deleteRecording()
    }

    fun onMicClicked() {
        viewModel.onMicClicked()
    }

    fun toggleRecording() {
        viewModel.toggleRecording()
    }

    fun stopRecording() {
        viewModel.stopRecording()
    }

    fun onPlayClicked() {
        viewModel.onPlayClicked()
    }

    fun onPausePlaybackClicked() {
        viewModel.onPausePlaybackClicked()
    }

    fun onSeek(position: Long) {
        viewModel.onSeek(position)
    }

    fun onTrim(start: Long, end: Long) {
        viewModel.onTrim(start, end)
    }

    fun toggleTrimMode() {
        viewModel.toggleTrimMode()
    }

    fun toggleExpand() {
        viewModel.toggleExpand()
    }

    fun onSkip(millis: Long) {
        viewModel.onSkip(millis)
    }

    fun onSaveClicked() {
        viewModel.onSaveClicked()
    }

    fun onResumeRecording() {
        viewModel.onResumeRecording()
    }

    fun onTrimUpdate(start: Long, end: Long) {
        viewModel.onTrimUpdate(start, end)
    }

    fun onReplaceClicked() {
        viewModel.onReplaceClicked()
    }

    fun onTrimCancel() {
        viewModel.onTrimCancel()
    }

    fun onRecordingClicked(recorder: UiWidget.VoiceRecorder) {
        viewModel.onRecordingClicked(recorder)
    }
}
