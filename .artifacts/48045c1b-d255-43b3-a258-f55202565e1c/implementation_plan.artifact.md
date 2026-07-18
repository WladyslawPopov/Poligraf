# Implementation Plan - Native iOS Navigation Experience

The goal is to move away from custom sidebar implementations on iPhone and adopt the standard iOS pattern: a **Native Bottom Sheet** for settings/drawer content on iPhone, while maintaining the **Native NavigationSplitView** for iPad.

## User Review Required

> [!IMPORTANT]
> **Architectural Shift:**
> 1. **iPhone (Compact)**: I will completely remove the custom "Gemini-style" sidebar and all associated `DragGesture` logic. Instead, I will use a native SwiftUI `.sheet` (Bottom Sheet).
>    - **Interaction**: Tapping the top-left button will open a native sheet with standard system animations.
>    - **Consistency**: This is the standard pattern for iOS apps when they need to show settings or additional views from a primary screen.
> 2. **iPad (Regular)**: I will keep the existing `NavigationSplitView`. It is the industry standard for large screens and already works perfectly in your project.
> 3. **Navigation Sync**: `IosNavigator` will continue to sync the drawer state with the Kotlin side, but on iPhone, it will manifest as a sheet instead of a column.

## Proposed Changes

### Navigation Layer

#### [MODIFY] [IosNavigator.swift](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/iosApp/iosApp/Navigation/IosNavigator.swift)
- **Simplify `IosNavigator`**:
    - Remove complexity around `preferredColumn` and `columnVisibility` for iPhone.
    - Introduce a simpler way to trigger the sheet on iPhone while keeping the split-view logic for iPad.
- **Refactor `IosNavHost`**:
    - **iPhone Layout**: Use a simple `NavigationStack` as the root. Add a `.sheet(isPresented:)` modifier that displays the `drawerView`.
    - **iPad Layout**: Use the native `NavigationSplitView`.
    - **Remove all custom `DragGesture` and `ZStack` offsets**.

### UI Components

#### [MODIFY] [MainView.swift](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/iosApp/iosApp/UI/MainView.swift)
- Update the leading toolbar button:
    - On iPhone, use a standard icon (like a gear or profile) or keep the hamburger if preferred, but it will now trigger the native sheet.
    - Ensure it works with the simplified navigator state.

#### [MODIFY] [DrawerView.swift](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/iosApp/iosApp/UI/Components/DrawerView.swift)
- Adjust the layout for the sheet format:
    - Use standard sheet padding and styling.
    - The "X" button can be removed if using a swipeable sheet, or kept as a trailing toolbar item.

## Verification Plan

### Manual Verification
- **iPhone**:
    - Tap the menu button: Native bottom sheet should appear with a standard iOS animation.
    - Swipe down the sheet: It should close natively following the finger.
    - Verify that the native back-swipe in `NavigationStack` works perfectly on sub-pages.
- **iPad**:
    - Verify the standard native `NavigationSplitView` toggle still works without any overlay or sheet.
