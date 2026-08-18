# 📑 Pivot Plan: VoiceStressAnalyzer (CMP Local-Only)

## Goal Description
Transition the project from a client-server AI-based "Lie Detector" to a local-only, science-based "Voice Stress Analyzer" using Compose Multiplatform (CMP). The app will focus on DSP (Digital Signal Processing) to detect vocal anomalies (Jitter, Pitch, RMS) without sending data to servers.

## User Review Required
- **Module Structure**: We are introducing `:app:ui-widgets` and repurposing `:app:sharedLogic` as the main CMP host.
- **Project Renaming**: We will update the application name and strings, but keep `:server` and `:core` folders as reference.
- **Native Navigation**: We will attempt to keep a native-like navigation experience using an `expect/actual` interface in the common module.

## Proposed Changes

### 1. Project Configuration

#### [MODIFY] [settings.gradle.kts](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/settings.gradle.kts)
- Add `:app:ui-widgets` to the included projects.

### 2. New UI Components Module

#### [NEW] [build.gradle.kts](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/ui-widgets/build.gradle.kts)
- Setup CMP library module for reusable UI components (Graphs, Oscillograms).

#### [NEW] [Oscillogram.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/ui-widgets/src/commonMain/kotlin/application/liedetector/widgets/Oscillogram.kt)
- Real-time Canvas-based drawing of the audio signal and stress markers.

### 3. Shared Logic & CMP UI Transition

#### [MODIFY] [build.gradle.kts](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/sharedLogic/build.gradle.kts)
- Update dependencies to include CMP and the new `:app:ui-widgets` module.
- Configure `commonMain` to host the main `App()` composable and page definitions.

#### [NEW] [App.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/sharedLogic/src/commonMain/kotlin/application/liedetector/presentation/App.kt)
- Root Composable with `Scaffold` and basic navigation shell.

### 4. Engine (DSP & Audio Capture)

#### [MODIFY] [build.gradle.kts](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/engine/build.gradle.kts)
- Focus on DSP and SQLDelight.
- Remove Ktor Client dependencies that are no longer needed (keep them in comments if necessary).

#### [NEW] [AudioAnalyzer.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/engine/src/commonMain/kotlin/application/liedetector/engine/dsp/AudioAnalyzer.kt)
- DSP logic: RMS calculation, Pitch detection (Autocorrelation), and Jitter estimation.

#### [NEW] [AudioCapture.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/engine/src/commonMain/kotlin/application/liedetector/engine/capture/AudioCapture.kt)
- `expect/actual` declarations for capturing PCM audio from the microphone.

### 5. Native App Integration

#### [MODIFY] [MainActivity.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/androidApp/src/main/kotlin/application/liedetector/MainActivity.kt)
- Update to host the CMP `App()` instead of the current Android-specific UI.

## Verification Plan

### Automated Tests
- Unit tests for DSP algorithms (RMS, Pitch, Jitter) with mock PCM data.
- SQLDelight migration tests.

### Manual Verification
- Deploy to Android Emulator/Device and verify audio waveform rendering.
- Verify that no network requests are made during analysis.
- Check cross-platform consistency of the UI.
