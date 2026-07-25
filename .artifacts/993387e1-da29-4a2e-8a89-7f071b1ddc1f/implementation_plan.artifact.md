# Implementation Plan: Final Architectural Polish & Design System Audit

The goal is to perform a comprehensive cleanup of the codebase to ensure high quality, production-ready logging, strict type safety on iOS, and 100% adherence to the Design System (removing hardcoded values).

## User Review Required

> [!IMPORTANT]
> **Logging Library:** We will integrate **Napier** for cross-platform logging. This replaces all `println` and `e.printStackTrace()` calls with a robust system that integrates with Logcat (Android) and `os_log` (iOS).
>
> **Design System Enforcement:** We will audit and fix all UI files to ensure dimensions, strings, and colors are exclusively derived from `DesignSystem`. We'll add missing tokens (`MAX_CONTENT_WIDTH`, `LABS_EMPTY_MESSAGE`) to the system.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/gradle/libs.versions.toml)
- Add `napier = "2.7.1"`.

#### [MODIFY] [sharedLogic/build.gradle.kts](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/sharedLogic/build.gradle.kts)
- Add `api(libs.napier)` to `commonMain`.

---

### Design System Expansion

#### [MODIFY] [DimenToken.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/ui-core/src/commonMain/kotlin/application/liedetector/uicore/theme/DimenToken.kt) & [ThemeDefaults.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/ui-core/src/commonMain/kotlin/application/liedetector/uicore/theme/ThemeDefaults.kt)
- Add `MAX_CONTENT_WIDTH` (600f).

#### [MODIFY] [StringToken.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/ui-core/src/commonMain/kotlin/application/liedetector/uicore/theme/StringToken.kt) & Providers
- Add `LABS_EMPTY_MESSAGE`.
- Update `AndroidResourceProvider.kt` and `IosResourceProvider.swift` with the new token.

---

### Shared Logic Polish

#### [MODIFY] [BaseViewModel.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/sharedLogic/src/commonMain/kotlin/application/liedetector/presentation/base/BaseViewModel.kt)
- Replace `e.printStackTrace()` with `Napier.e`.
- Add debug logging for `launchSafe` flows.

#### [MODIFY] [MainViewModel.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/sharedLogic/src/commonMain/kotlin/application/liedetector/presentation/main/MainViewModel.kt)
- Replace `println` with `Napier.d`.

---

### UI Audit & Polish (Removing Hardcode)

#### [MODIFY] [MainHost.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/androidApp/src/main/kotlin/application/liedetector/ui/screens/main/MainHost.kt) & [DebugHost.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/androidApp/src/main/kotlin/application/liedetector/ui/screens/debug/DebugHost.kt)
- Replace `16.dp` with `designSystem.dimen(DimenToken.MAIN_PADDING).dp`.
- Replace `12.dp` with `designSystem.dimen(DimenToken.WIDGET_SPACING).dp`.
- Replace hardcoded strings in Tabs with tokens.

#### [MODIFY] [MainView.swift](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/iosApp/iosApp/UI/MainView.swift) & [DebugView.swift](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/iosApp/iosApp/UI/DebugView.swift)
- Replace `600` with `designSystem.dimen(token: .maxContentWidth)`.
- Replace `spacing: 20` and hardcoded messages with design system equivalents.

#### [MODIFY] [AppScaffold.swift](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/iosApp/iosApp/UI/Components/AppScaffold.swift)
- Fully leverage SKIE types (e.g., `boolValue`) and remove manual casting.

---

## Verification Plan

### Automated Tests
- Run Gradle sync and build both platforms.

### Manual Verification
- Verify Napier logs appear in Logcat and Xcode.
- Verify UI remains identical (confirming that Design System values match the previously hardcoded ones).
