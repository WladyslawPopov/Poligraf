# Implementation Plan - Navigation Refinement & Bug Fixes

The goal is to fix two critical issues in the iOS navigation:
1. The sidebar menu blocking user input (cannot toggle dark mode).
2. A "white-ish grey" visual artifact/flash during the transition.

## Proposed Changes

### [Component: iOS App (UI Components)]

#### [MODIFY] [InteractivePager.swift](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/iosApp/iosApp/UI/Components/InteractivePager.swift)
- **Input Blocking Fix**:
    - Move the "Close Overlay" into the content layer's `ZStack`. This ensures the overlay only covers the main content area that has been shifted to the right, leaving the menu on the left fully interactive.
- **Visual Flash Fix**:
    - Refine the shadow: use a sharper, darker shadow with a smaller spread to avoid the "grey halo" effect.
    - Ensure `NavigationStack` background doesn't leak.
    - Adjust the background layering to ensure only one "true" background is visible at any time.

#### [MODIFY] [DrawerView.swift](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/iosApp/iosApp/UI/Components/DrawerView.swift)
- Ensure the background matches the main screen's theme precisely to prevent flickering during transitions.

## Verification Plan

### Manual Verification
- **Interactivity**: Open the drawer and toggle the "Dark Mode" switch. It should work without closing the drawer.
- **Tapping Content**: Tap the visible sliver of the main content on the right. It should close the drawer.
- **Visual Cleanliness**: Perform slow swipes and verify that the transition is dark and seamless, with no greyish flashes.
