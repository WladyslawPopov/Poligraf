# Refactoring VoiceRecorderRenderer

Refactor the `VoiceRecorderRenderer` to follow a clean architecture pattern (MVVM/MVI) by decoupling UI from logic, standardizing states, and breaking down the large composable into smaller, manageable components.

## User Review Required

- **ViewModel Integration**: The proposed `VoiceRecorderViewModel` will need to be connected to your recording engine (e.g., MediaRecorder/ExoPlayer). I will provide the VM structure and state management logic, but the actual interaction with hardware APIs should remain in your service or domain layer.
- **State Granularity**: I'm splitting the state into sub-states (Playback, Waveform, Trim) to avoid unnecessary recompositions.

## Proposed Changes

### UI Logic & State

#### [NEW] [VoiceRecorderUiState.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/androidApp/src/main/kotlin/application/liedetector/ui/components/widgets/VoiceRecorderUiState.kt)
Contains the data classes representing the UI state.
- `VoiceRecorderUiState`: Main state.
- `WaveformState`: State for the waveform visualizer.
- `TrimState`: State for the trimming tool.

#### [NEW] [VoiceRecorderViewModel.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/androidApp/src/main/kotlin/application/liedetector/ui/components/widgets/VoiceRecorderViewModel.kt)
Handles UI events and transforms them into state updates.
- Contains logic for timer formatting.
- Handles amplitude processing for the waveform.
- Manages playback and recording status.

### UI Components

#### [NEW] [VoiceRecorderComponents.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/androidApp/src/main/kotlin/application/liedetector/ui/components/widgets/VoiceRecorderComponents.kt)
Small, reusable components:
- `RecorderTimer`: Monospaced timer display.
- `WaveformVisualizer`: Renders the waveform bars and ruler.
- `TrimSlider`: The interactive trimming tool.
- `PlaybackControls`: Play/Pause/Skip buttons.
- `RecorderToolbar`: Top action bar.

#### [MODIFY] [VoiceRecorderRenderer.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/androidApp/src/main/kotlin/application/liedetector/ui/components/widgets/VoiceRecorderRenderer.kt)
- Simplify to a "dumb" container that accepts state and propagates events.
- Split into `CollapsedVoiceRecorder` and `ExpandedVoiceRecorder` using the new components.

## Verification Plan

### Manual Verification
- Verify the waveform scrolling and dragging behavior.
- Ensure the trim handles correctly update the selection.
- Check that the timer formats correctly in different states (recording vs playback).
- Verify the layout matches the existing "Apple-style" design.
