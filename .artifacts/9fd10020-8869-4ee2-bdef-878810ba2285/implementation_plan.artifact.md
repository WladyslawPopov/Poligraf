# Fix Analyzer Appended Recording and History Tape

The user reported that when resuming a recording after a pause, the "history tape" at the bottom appears empty or doesn't show previous data. This is likely caused by the `frameHistory` and `timelineMarkers` in `AnalyzerViewModel` not being populated correctly when the ViewModel is recreated during an active recording session (e.g., after the app was in the background). Additionally, the duration/timestamp calculation in `AnalyzerRepositoryImpl` is based on the system clock, which can lead to collisions and data loss during processing lags.

## User Review Required

> [!NOTE]
> The fix involves reloading session history from the database if the ViewModel is initialized while a recording is already in progress. This ensures that the "rewind" functionality (history tape) works correctly even after process recreation.

## Proposed Changes

### [Presentation Layer]

#### [MODIFY] [AnalyzerViewModel.kt](file:///Users/krampus/AndroidStudioProjects/KMP/Poligraf/app/sharedLogic/src/commonMain/kotlin/application/poligraf/presentation/analyzer/AnalyzerViewModel.kt)
- Update `onAppear()` to load session history from the database if `repository.isRecording` is true but `frameHistory` is empty.
- Ensure `processAnomalyMarker` uses the frame's own timestamp instead of the potentially slightly desynced `durationMillis` from the repository.

### [Data Layer]

#### [MODIFY] [AnalyzerRepositoryImpl.kt](file:///Users/krampus/AndroidStudioProjects/KMP/Poligraf/app/data/src/commonMain/kotlin/application/poligraf/data/repository/AnalyzerRepositoryImpl.kt)
- Switch duration calculation from `nowAsEpochMilliseconds()` to a sample-based counter.
- Add `windowsProcessedInStretch` to track progress within a single recording "stretch" (start to pause, or resume to pause).
- Reset this counter in `startAnalysis` and `resumeAnalysis`.
- This ensures perfectly sequential timestamps (100ms increments) and avoids issues with processing jitter or system clock drift.

## Verification Plan

### Automated Tests
- Verify `AnalyzerRepositoryImpl` produces sequential timestamps even with simulated processing delays.
- Verify `AnalyzerViewModel` loads history correctly when `onAppear` is called during recording.

### Manual Verification
1. Start recording.
2. Pause.
3. Resume.
4. Verify history tape shows data from before the pause.
5. Simulate process death (or just call `onAppear` on a fresh VM while repo is recording) and verify history tape is restored.
