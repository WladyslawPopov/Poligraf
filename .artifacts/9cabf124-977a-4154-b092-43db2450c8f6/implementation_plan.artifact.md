# Full-Featured Voice Recorder Implementation (Apple Style)

The goal is to implement a voice recorder that matches the user experience of the native Apple Voice Memos app. This includes a persistent bottom recorder in the history view that can be expanded, real-time waveform visualization, and advanced editing features (Replace/Trim).

## Proposed Changes

### [iOS] UI Components

#### [MODIFY] [RecordingsHistoryView.swift](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/iosApp/iosApp/UI/Screens/RecordingHistory/RecordingsHistoryView.swift)
- Remove the floating mic button and the `.sheet` based recorder.
- Add `VoiceRecorderView` as a persistent bottom component within a `ZStack`.
- Implement a custom bottom sheet behavior that responds to the `activeRecorder.isExpanded` state.

#### [MODIFY] [VoiceRecorderView.swift](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/iosApp/iosApp/UI/Widgets/recorder/VoiceRecorderView.swift)
- Add a "Collapsed" mode that shows only the record button and a minimal timer/waveform.
- Improve animations when transitioning between collapsed and expanded states.
- Adjust layout to be more compact when not fully expanded.

#### [MODIFY] [VoiceRecorderProfessionalWaveformView.swift](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/iosApp/iosApp/UI/Widgets/recorder/VoiceRecorderProfessionalWaveformView.swift)
- Refine the waveform rendering to match Apple's aesthetic (red bars during recording, specific spacing).
- Optimize drawing for high frame rates during recording.

### [Shared] Presentation Logic

#### [MODIFY] [RecordingsHistoryViewModel.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/sharedLogic/src/commonMain/kotlin/application/liedetector/presentation/recordingHistory/RecordingsHistoryViewModel.kt)
- Ensure the `activeRecorder` state correctly manages the `isExpanded` flag based on user interactions.
- Add logic to automatically expand the recorder when recording starts if needed.
- Improve synchronization of amplitudes to the UI to ensure smooth visualization.

### [Native] Engine

#### [MODIFY] [IosAudioRecorder.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/engine/src/nativeMain/kotlin/application/liedetector/engine/io/audio/IosAudioRecorder.kt)
- Refine the amplitude calculation to be more reactive.
- Ensure the "Replace" functionality works seamlessly by merging audio segments accurately.

## Verification Plan

### Manual Verification
- Start recording from the history view.
- Verify the recorder expands correctly.
- Check waveform smoothness and accuracy.
- Test "Replace" by pausing, seeking back, and recording over.
- Test "Trim" functionality.
- Verify the recording appears in the history list immediately after saving.
