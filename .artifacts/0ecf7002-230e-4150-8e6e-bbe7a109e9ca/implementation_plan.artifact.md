# Aligning Android Architecture with iOS (MainHost as Global Container)

The goal is to move background visualization and navigation drawer logic to the `MainHost` composable on Android, ensuring consistent architectural structure with the iOS implementation.

## Proposed Changes

### [app:androidApp]

#### [MODIFY] [MainActivity.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/androidApp/src/main/kotlin/application/liedetector/MainActivity.kt)
- Remove `ScalesBackground` and the glass veil. Keep the file as a minimal host for `RootComponent` and theme initialization.

#### [MODIFY] [MainHost.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/androidApp/src/main/kotlin/application/liedetector/ui/screens/main/MainHost.kt)
- Wrap the entire `Scaffold` (and `NativeNavHost`) in a `ZStack` (or `Box` with overlapping layers).
- Move `ScalesBackground` inside `MainHost`.
- Apply the glass veil (`Color.Black.copy(alpha = 0.15f)`) inside `MainHost` so the background always persists globally for the Main screen.

## Verification Plan

### Manual Verification
- Check that the background animation persists and is not re-initialized during navigation.
- Confirm Drawer behavior is consistent with the iOS implementation.
- Ensure the overall layout maintains the design system's spacing and theme tokens.
