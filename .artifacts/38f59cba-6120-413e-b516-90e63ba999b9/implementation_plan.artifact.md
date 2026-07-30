# Implementation Plan - Reverting to Standard Native Tabs (iOS)

The user wants to use the standard native tab component (Picker with segmented style) on iOS instead of the custom implementation, while keeping the glass effect and swipe functionality.

## Proposed Changes

### [iosApp] UI Components

#### [MODIFY] [AppTabs.swift](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/iosApp/iosApp/UI/Components/AppTabs.swift)
- Replace the custom `HStack` and `Button` logic with a standard SwiftUI `Picker`.
- Apply `.pickerStyle(.segmented)`.
- Use `.background(.ultraThinMaterial)` and established glass tokens to keep it styled correctly without breaking the "standard component" feel.

### [iosApp] Debug Screen

#### [MODIFY] [DebugView.swift](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/iosApp/iosApp/UI/DebugView.swift)
- Ensure the `Binding` correctly triggers animations and syncs with the `TabView`.

### [androidApp] Verification
- Confirm that `GlassSegmentedTabRow` on Android uses the standard `SingleChoiceSegmentedButtonRow`. (Checked: it does).

## Verification Plan

### Manual Verification
- Run the iOS app.
- **Verify**:
    - The tab header uses the standard iOS segmented control (with the smooth sliding pill).
    - The background of the header remains "glassy".
    - Swiping between pages in the `TabView` updates the `Picker` correctly.
    - Clicking the `Picker` updates the `TabView` correctly.
