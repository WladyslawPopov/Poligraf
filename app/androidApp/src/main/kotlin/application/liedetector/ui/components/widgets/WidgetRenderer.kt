package application.liedetector.ui.components.widgets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import application.liedetector.presentation.recording.RecordingComponent
import application.liedetector.uicore.actions.WidgetAction
import application.liedetector.uicore.widgets.UiWidget

@Composable
fun WidgetRenderer(
    widget: UiWidget,
    onAction: (WidgetAction) -> Unit,
    component: RecordingComponent? = null
) {
    when (widget) {
        is UiWidget.WelcomeText -> {
            WelcomeTextRenderer(widget)
        }
        
        is UiWidget.SubjectSlider -> {
            SubjectSliderRenderer(widget, onAction)
        }
        
        is UiWidget.SubjectList -> {
            SubjectListRenderer(widget, onAction)
        }

        is UiWidget.VoiceRecorder -> {
            VoiceRecorderRenderer(
                widget = widget,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                onToggle = { component?.toggleRecording() },
                onStop = { component?.stopRecording() },
                onPlay = { component?.onPlayClicked() },
                onPause = { component?.onPausePlaybackClicked() },
                onSeek = { component?.onSeek(it) },
                onTrimUpdate = { start, end -> component?.onTrimUpdate(start, end) },
                onSave = { component?.onSaveClicked() },
                onResume = { component?.onResumeRecording() },
                onToggleTrim = { component?.toggleTrimMode() },
                onSkip = { component?.onSkip(it) },
                onToggleExpand = { component?.toggleExpand() },
                onTrimCancel = { component?.onTrimCancel() },
                onTrimApply = { start, end -> component?.onTrim(start, end) }
            )
        }
        
        else -> {
            Box(modifier = Modifier.size(0.dp))
        }
    }
}
