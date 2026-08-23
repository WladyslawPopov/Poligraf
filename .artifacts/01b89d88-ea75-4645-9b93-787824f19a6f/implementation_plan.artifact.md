# Native iOS Audio Recorder Implementation

This plan replaces the legacy Swift-based audio bridge with a native Kotlin implementation using `AVFoundation`. This aligns the iOS recording logic with the Android implementation, providing a consistent `Flow<ShortArray>` of PCM data directly from the KMP module.

## User Review Required

> [!IMPORTANT]
> This change replaces the dependency on `NativeVoiceRecorderEngine.swift` for capturing PCM data. If your iOS app still relies on `NativeVoiceRecorderEngine.swift` for other features (like local file recording or specific UI feedback not yet in KMP), you might need to keep it running in parallel or migrate those features as well.

## Proposed Changes

### Engine Module (nativeMain)

#### [MODIFY] [IosAudioRecorder.kt](file:///Users/krampus/AndroidStudioProjects/KMP/Poligraf/app/engine/src/nativeMain/kotlin/application/poligraf/engine/io/audio/IosAudioRecorder.kt)
- Remove the bridge interface `IosAudioRecorder` and its delegate if they are no longer needed for the core `AudioRecorder` functionality.
- Implement `IosAudioRecorderImpl` using `AVAudioEngine`.
- Use `AVAudioSession` to configure the audio hardware for recording.
- Install a tap on the `inputNode` to capture raw audio buffers.
- Convert `Float32` PCM buffers to `Int16` (`ShortArray`) to match the `AudioRecorder` interface.

#### [MODIFY] [getAudioRecorder.native.kt](file:///Users/krampus/AndroidStudioProjects/KMP/Poligraf/app/engine/src/nativeMain/kotlin/application/poligraf/engine/io/audio/common/getAudioRecorder.native.kt)
- Ensure the factory function returns the new `IosAudioRecorderImpl`.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:engine:assembleDebug` to verify that the Kotlin/Native code compiles correctly with `AVFoundation` interop.

### Manual Verification
- Deploy the `iosApp` to a physical device or simulator.
- Start a recording session and verify that the `rawAudioFlow` in the KMP engine receives data.
- Verify that the UI visualizations (if connected to the engine) react to voice input.
