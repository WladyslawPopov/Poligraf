# Align iOS Voice Recorder and History with Android

Synchronize the iOS implementation of the voice recorder and recordings history with the Android version, ensuring visual and functional parity. The iOS recorder will be integrated using the `AppSheetContainer` and modern SwiftUI sheet patterns.

## User Review Required

> [!IMPORTANT]
> The `AppSheetContainer` will be used to wrap the recorder content. I will adjust the layout of `VoiceRecorderView` to fit into a bottom sheet presentation.
> The "Big Red Button" pulse animation will be implemented using SwiftUI's `PhaseAnimator` or `withAnimation` to match Android's pulse.

## Proposed Changes

### iOS UI Layer

#### [MODIFY] [RecordingsHistoryView.swift](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/iosApp/iosApp/UI/Screens/RecordingHistory/RecordingsHistoryView.swift)
- Update `RecordingRow` to match `RecordingListItem` (Glass background, layout).
- Enhance the "Big Red Button" with pulse animation and styling.
- Replace the manual `ZStack` recorder overlay with a `.sheet` or `.fullScreenCover` using `AppSheetContainer` (or a similar bottom-anchored view if appropriate for the "peek" behavior).
- *Correction*: Android uses a `BottomSheetScaffold` which allows a "peek" state. iOS doesn't have a perfect native equivalent for "peek + list interaction" without custom implementation or `presentationDetents`. I will use `presentationDetents` to simulate the peek and expanded states.

#### [MODIFY] [VoiceRecorderView.swift](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/iosApp/iosApp/UI/Widgets/VoiceRecorderView.swift)
- Re-implement `collapsedContent` and `expandedContent` to match `VoiceRecorderRenderer.kt`.
- Update `MiniWaveformView` for better bar rendering.
- Overhaul `ProfessionalWaveformView`:
    - Add the time ruler (ticks and labels).
    - Improve dragging/seeking logic.
    - Add the playhead with circles at ends.
- Implement `MiniTrimOverview` with interactive handles.
- Add Discard, Menu, and Save buttons to the expanded header.

## Verification Plan

### Manual Verification
1.  Deploy to iOS Simulator/Device.
2.  Open Recordings History.
3.  Verify the "Big Red Button" pulses and starts recording.
4.  Verify the recorder appears in a bottom sheet.
5.  Test collapsing/expanding the recorder.
6.  Compare the visual appearance of the waveform and ruler with the Android version.
7.  Test trimming mode and seeking on the waveform.
8.  Verify recording history list items match the Android design (Glass base, dividers).
