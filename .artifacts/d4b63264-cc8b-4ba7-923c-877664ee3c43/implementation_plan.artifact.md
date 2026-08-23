# Implement iOS Audio Streaming Parity with Android

The goal is to ensure the iOS audio recording mechanism matches the Android implementation in terms of data format (44100Hz, Mono, ShortArray) and streaming behavior using `SharedFlow`.

## User Review Required

> [!IMPORTANT]
> The current iOS implementation using `AVAudioEngine` may capture audio at the device's native sample rate (often 48kHz). The Android implementation explicitly requests 44.1kHz. We will add a resampler (AVAudioConverter) on iOS to ensure the output flow is consistently 44.1kHz.

## Proposed Changes

### Audio Engine Component

#### [MODIFY] [IosAudioRecorder.kt](file:///Users/krampus/AndroidStudioProjects/KMP/Poligraf/app/engine/src/nativeMain/kotlin/application/poligraf/engine/io/audio/IosAudioRecorder.kt)
- Refactor `IosAudioRecorderImpl` to use `AVAudioConverter` if the input sample rate differs from `AudioConstants.SAMPLING_RATE`.
- Ensure robust conversion from `Float32` to `Int16`.
- Improve error handling for `AVAudioSession` activation and engine start.
- Use `tryEmit` or a more efficient emission strategy to avoid excessive `scope.launch` calls if possible, or ensure the scope is appropriate.

## Verification Plan

### Manual Verification
- Deploy to an iOS device/simulator.
- Verify that `rawAudioFlow` emits data.
- Check if the sample rate on the receiving end (Analyzer) matches 44.1kHz (data should not be pitched up or down).
