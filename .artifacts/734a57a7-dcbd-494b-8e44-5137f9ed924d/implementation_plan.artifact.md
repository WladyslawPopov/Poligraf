# Implementation Plan - Voice Recorder Fixes

Fixing issues with audio trimming, file loading, and appending (resuming) in the Android voice recorder implementation.

## Proposed Changes

### [Component Name] Engine - Audio Recording

#### [MODIFY] [AndroidAudioRecorder.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/engine/src/androidMain/kotlin/application/liedetector/engine/io/audio/AndroidAudioRecorder.kt)
- **Fix Trimming Logic**:
    - Implement a more robust `trim` function using `MediaExtractor` and `MediaMuxer`.
    - Ensure samples are skipped correctly until `startMillis` is reached.
    - Handle presentation timestamps correctly to avoid negative values that cause `MediaMuxer` to crash.
    - Create a fresh copy of the amplitudes list when trimming to avoid issues with sub-list views.
- **Improve File Merging**:
    - Enhance `mergeAudioFiles` to ensure seamless concatenation of audio segments.
    - Calculate `startTimeOffsetUs` more precisely.
- **Robustness**:
    - Add `try-catch` blocks around `player?.stop()` and other potentially failing media calls.
    - Improve logging to help identify why a file might fail to load.
- **State Management**:
    - Ensure `loadFile` properly synchronizes all internal `StateFlow`s.

### [Component Name] Presentation - Recording History

#### [MODIFY] [RecordingsHistoryViewModel.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/sharedLogic/src/commonMain/kotlin/application/liedetector/presentation/recordingHistory/RecordingsHistoryViewModel.kt)
- **Fix Loading Logic**:
    - Ensure `onRecordingClicked` correctly prepares the engine and UI state.
    - Add a check for file existence (via logs/Napier) before loading.
- **Fix Trimming Interaction**:
    - Ensure `onTrim` correctly updates the UI and resets the engine state with the new trimmed file.
- **Fix Resuming/Appending**:
    - Ensure `onResumeRecording` correctly sets the engine to "append mode" at the end of the current file.

## Verification Plan

### Automated Tests
- I will verify the build succeeds after changes.
- (Note: Testing `MediaMuxer` logic ideally requires a device/emulator).

### Manual Verification
- Deploy to Android device/emulator.
- Record a voice clip, save it.
- Open it from history, verify it plays.
- Resume recording (append), verify the length increases and both parts are audible.
- Use the trim handles and click "Trim", verify the file is correctly cut and still playable.
