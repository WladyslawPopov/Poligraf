# Refactor UI Core: Split Tokens and Extract UI States

This plan aims to improve the organization of the `ui-core` module by splitting the large `ThemeTokens.kt` file into smaller, specialized files and moving UI-related states to a separate package.

## Proposed Changes

### [UI Core (Shared)]

#### [NEW] [ColorToken.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/ui-core/src/commonMain/kotlin/application/liedetector/uicore/theme/ColorToken.kt)
- Contains the `ColorToken` enum.

#### [NEW] [DimenToken.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/ui-core/src/commonMain/kotlin/application/liedetector/uicore/theme/DimenToken.kt)
- Contains the `DimenToken` enum.

#### [NEW] [IconToken.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/ui-core/src/commonMain/kotlin/application/liedetector/uicore/theme/IconToken.kt)
- Contains the `IconToken` enum.

#### [NEW] [StringToken.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/ui-core/src/commonMain/kotlin/application/liedetector/uicore/theme/StringToken.kt)
- Contains the `StringToken` enum.

#### [NEW] [TypographyToken.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/ui-core/src/commonMain/kotlin/application/liedetector/uicore/theme/TypographyToken.kt)
- Contains the `TypographyToken` enum.

#### [NEW] [UiState.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/ui-core/src/commonMain/kotlin/application/liedetector/uicore/state/UiState.kt)
- New package: `application.liedetector.uicore.state`.
- Contains `ErrorType`, `ToastType`, and `ToastState`.

#### [DELETE] [ThemeTokens.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/ui-core/src/commonMain/kotlin/application/liedetector/uicore/theme/ThemeTokens.kt)
- Remove the old monolithic file.

### [Import Updates]

#### [MODIFY] Multiple Files
- Update imports of `ErrorType`, `ToastType`, and `ToastState` from `application.liedetector.uicore.theme` to `application.liedetector.uicore.state`.
- Affected files include:
    - `BaseViewModel.kt`
    - `ServerErrorException.kt`
    - `ErrorView.kt`
    - `ToastView.kt`
    - `AppScaffold.kt` (Android & iOS)
    - `MainView.swift` (if applicable, though usually bridged)

## Verification Plan

### Automated Tests
- Run `gradle :ui-core:assemble` to verify the module builds correctly with new file structure.
- Run `gradle :app:androidApp:assembleDebug` to ensure all imports are fixed.

### Manual Verification
- Check that the project builds in Android Studio.
- Verify that no duplicate definitions exist.
