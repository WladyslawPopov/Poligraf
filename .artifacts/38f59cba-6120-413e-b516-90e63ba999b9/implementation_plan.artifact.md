# Implementation Plan - iOS Theme Switching Fix

The user reported that theme switching on iOS is "incomplete". This is because the `DesignSystem` instance in `ContentView` was initialized once and did not react to changes in the `IosNavigator.isDark` state.

## Proposed Changes

### [iosApp] Root UI

#### [MODIFY] [ContentView.swift](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/iosApp/iosApp/UI/ContentView.swift)
- Change `designSystem` from a private `let` to a private computed property.
- The computed property will instantiate a new `DesignSystem` using `navigator.isDark` every time it's accessed.
- This ensures that when the `navigator` state changes, a new `DesignSystem` object with the correct theme flag is passed down to all child views (`MainView`, `DebugView`, `AppScaffold`, etc.).
- Ensure `isDebug` flag is also correctly derived in the computed property.

## Verification Plan

### Manual Verification
- Run the iOS app.
- Open the Drawer (Settings).
- Toggle the "Dark Mode" switch.
- **Verify**:
    - The background color changes immediately (e.g., from Dark Anthracite to Blue-Gray).
    - Text colors invert correctly (White <-> Slate).
    - All "glass" components update their materials and tints.
    - Navigation remains functional and the theme state is preserved across screens.
