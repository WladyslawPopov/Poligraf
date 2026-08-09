import SwiftUI
import SharedLogic

struct WidgetView: View {
    let widget: UiWidget
    let designSystem: DesignSystem
    let onAction: (WidgetAction) -> Void
    var recordingComponent: RecordingComponent? = nil
    
    var body: some View {
        Group {
            if let welcome = widget as? UiWidget.WelcomeText {
                WelcomeTextView(widget: welcome, designSystem: designSystem)
            } else if let slider = widget as? UiWidget.SubjectSlider {
                SubjectSliderView(widget: slider, designSystem: designSystem, onAction: onAction)
            } else if let list = widget as? UiWidget.SubjectList {
                SubjectListView(widget: list, designSystem: designSystem, onAction: onAction)
            } else if let recorder = widget as? UiWidget.VoiceRecorder {
                VoiceRecorderView(
                    widget: recorder,
                    designSystem: designSystem,
                    onToggle: { recordingComponent?.toggleRecording() },
                    onStop: { recordingComponent?.stopRecording() },
                    onPlay: { recordingComponent?.onPlayClicked() },
                    onPause: { recordingComponent?.onPausePlaybackClicked() },
                    onSeek: { recordingComponent?.onSeek(position: $0) },
                    onTrimUpdate: { recordingComponent?.onTrimUpdate(start: $0, end: $1) },
                    onSave: { recordingComponent?.onSaveClicked() },
                    onResume: { recordingComponent?.onResumeRecording() },
                    onToggleTrim: { recordingComponent?.toggleTrimMode() },
                    onSkip: { recordingComponent?.onSkip(millis: $0) },
                    onTrimCancel: { recordingComponent?.onTrimCancel() },
                    onTrimApply: { recordingComponent?.onTrim(start: $0, end: $1) },
                    onReplace: { recordingComponent?.onReplaceClicked() },
                    onSave: { recordingComponent?.onSaveClicked() },
                    onResume: { recordingComponent?.onResumeRecording() }
                )
            } else {
                EmptyView()
            }
        }
    }
}
