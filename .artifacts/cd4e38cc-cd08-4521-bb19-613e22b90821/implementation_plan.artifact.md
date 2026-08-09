# Professional Voice Recorder Widget Implementation

Improve the existing voice recording widget to support real-time visualization, playback, seeking, and basic trimming.

## Proposed Changes

### [Core Logic & State]

#### [MODIFY] [AudioRecorder.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/engine/src/commonMain/kotlin/application/liedetector/engine/io/audio/AudioRecorder.kt)
- Expand interface to support playback (`play()`, `pausePlayback()`, `seekTo(position)`) and trimming (`trim(start, end)`).
- Add `playbackPositionMillis` and `isPlaying` StateFlows.

#### [MODIFY] [UiWidget.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/ui-core/src/commonMain/kotlin/application/liedetector/uicore/widgets/UiWidget.kt)
- Update `VoiceRecorder` data class to include playback state: `playbackPositionMillis`, `isPlaying`, `isTrimming`, `trimRange`.

#### [MODIFY] [RecordingViewModel.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/sharedLogic/src/commonMain/kotlin/application/liedetector/presentation/recording/RecordingViewModel.kt)
- Integrate new `AudioRecorder` features into the ViewModel.
- Handle seek events and trimming logic.
- Ensure `amplitudes` list is not truncated during recording.

---

### [Platform Implementations]

#### [MODIFY] [AndroidAudioRecorder.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/engine/src/androidMain/kotlin/application/liedetector/engine/io/audio/AndroidAudioRecorder.kt)
- Store full amplitude history.
- Implement playback using `MediaPlayer`.
- Implement basic trimming logic.

#### [MODIFY] [IosAudioRecorder.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/engine/src/nativeMain/kotlin/application/liedetector/engine/io/audio/IosAudioRecorder.kt)
- Store full amplitude history.
- Implement playback using `AVAudioPlayer`.
- Implement basic trimming logic using `AVAssetExportSession`.

---

### [UI Components]

#### [MODIFY] [VoiceRecorderRenderer.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/androidApp/src/main/kotlin/application/liedetector/ui/components/widgets/VoiceRecorderRenderer.kt)
- Replace static waveform with a `ScrollableWaveform`.
- Add a seek slider and professional playback controls (Rewind, Play/Pause, Forward, Trim).
- Implement interactive trimming markers.

#### [MODIFY] [VoiceRecorderView.swift](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/iosApp/iosApp/UI/Widgets/VoiceRecorderView.swift)
- Parallel updates for iOS: scrollable waveform, playback controls, and trimming UI.

## Verification Plan

### Automated Tests
- Unit tests for `RecordingViewModel` to verify state transitions (Recording -> Finished -> Playing).

### Manual Verification
1.  **Recording**: Start recording, verify waveform updates in real-time and scales correctly.
2.  **Playback**: Stop recording, press play, verify audio plays and waveform/slider seeks.
3.  **Seeking**: Drag the slider/waveform during playback, verify audio seeks correctly.
4.  **Trimming**: Enter trim mode, adjust markers, save, and verify the resulting file is shorter.
5.  **Platforms**: Test on both Android Emulator and iOS Simulator.
