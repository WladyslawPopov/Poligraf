# Implementation Plan - Professional Voice Recorder Widget

This plan covers the implementation of a full-featured voice recorder widget with scrollable waveforms, playback controls, and basic editing (trimming) functionality across Android (Compose) and iOS (SwiftUI).

## Proposed Changes

### [Component] Audio Engine (Common & Platform)

#### [MODIFY] [AudioRecorder.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/engine/src/commonMain/kotlin/application/liedetector/engine/io/audio/AudioRecorder.kt)
- Ensure it supports capturing full amplitude history (done).

#### [NEW] [AudioProcessor.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/engine/src/commonMain/kotlin/application/liedetector/engine/io/audio/AudioProcessor.kt)
- Define interface for audio editing: `trim(inputPath: String, startTime: Long, endTime: Long): String`.

#### [MODIFY] [AndroidModule.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/androidApp/src/main/kotlin/application/liedetector/di/AndroidModule.kt)
- Register `AudioPlayer` and `AudioProcessor`.

---

### [Component] Shared Logic (Presentation)

#### [MODIFY] [UiWidget.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/ui-core/src/commonMain/kotlin/application/liedetector/uicore/widgets/UiWidget.kt)
- Expand `VoiceRecorder` model:
    - Add `playbackPositionMillis: Long`.
    - Add `trimRange: LongRange?`.
    - Add `Status.PREVIEWING` and `Status.EDITING`.

#### [MODIFY] [RecordingViewModel.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/sharedLogic/src/commonMain/kotlin/application/liedetector/presentation/recording/RecordingViewModel.kt)
- Integrate `AudioPlayer`.
- Add functions: `playPreview()`, `pausePreview()`, `seekTo(millis)`, `setTrim(range)`, `saveTrimmed()`.

#### [MODIFY] [RootComponent.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/sharedLogic/src/commonMain/kotlin/application/liedetector/presentation/root/RootComponent.kt)
- Inject `AudioPlayer` and pass it to `RecordingViewModel`.

---

### [Component] Android UI (Compose)

#### [MODIFY] [VoiceRecorderRenderer.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/androidApp/src/main/kotlin/application/liedetector/ui/components/widgets/VoiceRecorderRenderer.kt)
- Implement `ScrollableWaveform` using `Canvas` and `Modifier.scrollable`.
- Add `Slider` for playback navigation.
- Add `TrimHandles` UI.
- Update control buttons based on new states.

---

### [Component] iOS UI (SwiftUI)

#### [MODIFY] [VoiceRecorderView.swift](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/iosApp/iosApp/UI/Widgets/VoiceRecorderView.swift)
- Implement `ScrollableWaveformView` in SwiftUI.
- Add playback controls and trimming UI elements.

## Verification Plan

### Automated Tests
- Unit tests for `RecordingViewModel` state transitions.

### Manual Verification
1. Start recording, verify real-time waveform.
2. Stop recording, enter preview mode.
3. Play and seek using the waveform/slider.
4. Adjust trim handles and save.
5. Verify the resulting file has the correct duration.
