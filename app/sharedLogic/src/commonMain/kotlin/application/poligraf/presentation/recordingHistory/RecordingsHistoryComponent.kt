package application.poligraf.presentation.recordingHistory

import androidx.compose.runtime.Stable
import application.poligraf.engine.component.ComponentContext
import application.poligraf.uicore.widgets.UiWidget
import application.poligraf.uicore.widgets.VoiceRecorder

@Stable
class RecordingsHistoryComponent(
    val context: ComponentContext,
    val viewModel: RecordingsHistoryViewModel
) {
    fun goBack() {
        viewModel.goBack()
    }

    fun handleVoiceAction(action: VoiceRecorderAction) {
        viewModel.handleAction(action)
    }

    fun deleteRecording() {
        viewModel.deleteRecording()
    }

    fun onMicClicked() {
        viewModel.onMicClicked()
    }

    fun loadContent() {
        // ViewModel already observes data, but we can trigger a manual sync if needed
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

    fun onUploadFromFileClicked() {
        viewModel.onUploadFromFileClicked()
    }

    fun onRecordingClicked(recorder: VoiceRecorder) {
        viewModel.onRecordingClicked(recorder)
    }
}
