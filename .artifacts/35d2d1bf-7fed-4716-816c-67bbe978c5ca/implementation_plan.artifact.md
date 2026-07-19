# Complete Legacy Navigator Cleanup

This plan focuses on removing all legacy navigation code (`AppNavigator`, `DefaultAppNavigator`, etc.) and simplifying the `navigation` module to only support `NavigationContext` and `NavRoute`.

## Proposed Changes

### [Navigation (Shared)]

#### [MODIFY] [NavigationContext.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/navigation/src/commonMain/kotlin/application/liedetector/navigation/NavigationContext.kt)
- Add `interface NavRoute`.
- Remove `navigator: AppNavigator<Any>?` from `NavigationContext` interface.
- Remove `navigator` parameter and property from `DefaultNavigationContext`.

#### [DELETE] [Navigator.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/navigation/src/commonMain/kotlin/application/liedetector/navigation/Navigator.kt)
- Remove entire file containing legacy stack management.

#### [DELETE] [NativeNavStack.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/navigation/src/nativeMain/kotlin/application/liedetector/navigation/NativeNavStack.kt)
- Remove legacy iOS wrapper.

### [Navigation (Android)]

#### [NEW] [NavigatorUtils.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/navigation/src/androidMain/kotlin/application/liedetector/navigation/NavigatorUtils.kt)
- Implement `ComponentActivity.navigationContext()` extension without legacy navigator parameters.

### [Shared Logic]

#### [MODIFY] [IosComponentFactory.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/sharedLogic/src/nativeMain/kotlin/application/liedetector/di/IosComponentFactory.kt)
- Update `DefaultNavigationContext` initialization to reflect the simplified constructor.

### [Android Implementation]

#### [MODIFY] [MainActivity.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/androidApp/src/main/kotlin/application/liedetector/MainActivity.kt)
- Ensure all calls to `navigationContext()` are clean.

## Verification Plan

### Automated Tests
- Run `gradle :navigation:assemble` to ensure the simplified module builds.
- Run `gradle :app:androidApp:assembleDebug` to verify no legacy navigator imports remain.

### Manual Verification
- Verify that the app still navigates correctly on both platforms using the new native stack.
