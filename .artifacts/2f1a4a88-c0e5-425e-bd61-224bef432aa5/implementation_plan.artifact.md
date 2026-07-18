# Implementation Plan - Fix Native NavigationSplitView Configuration

The goal is to fix the native `NavigationSplitView` implementation to behave correctly on both iPhone and iPad, following the user's specific requirements: Sidebar closed by default on iPhone, no duplicate buttons on iPad, and correct theme application in the sidebar.

## User Review Required

> [!IMPORTANT]
> I will fix the iPhone "white screen" and "inaccessible menu" issues by properly managing the `preferredCompactColumn` state. I will also ensure that the custom toggle button only appears on iPhone, avoiding duplication on iPad.

## Proposed Changes

### [Component: iOS App (Navigation)]

#### [MODIFY] [IosNavigator.swift](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/iosApp/iosApp/Navigation/IosNavigator.swift)
- **Compact Navigation Logic**:
    - Update `toggleDrawer()` to switch `preferredCompactColumn` between `.sidebar` and `.detail` when on iPhone. This is the only way to programmatically switch columns in a compact `NavigationSplitView`.
- **Theme Consistency**:
    - Ensure `.environment(\.colorScheme, ...)` is applied correctly to the entire split view.
- **Background Integrity**:
    - Remove the `.background(Color.clear)` on the detail stack if it causes white leaks.
    - Set the `NavigationSplitView` background to the theme color.

### [Component: iOS App (UI)]

#### [MODIFY] [MainView.swift](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/iosApp/iosApp/UI/MainView.swift)
- **Adaptive Toolbar**:
    - Move the custom sidebar toggle button into an `if sizeClass == .compact` block. This removes the "double button" issue on iPad while providing a way to open the menu on iPhone.
    - Re-enable the native navigation bar appearance but with the correct theme color to ensure the "magnificent native blur" is preserved without the "ugly stripe".

#### [MODIFY] [DrawerView.swift](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/iosApp/iosApp/UI/Components/DrawerView.swift)
- **Fix Toggle Color**: Use `.tint(IosTheme.color(.accentEnergy, ...))` on the theme switcher.
- **Fix Background**: Explicitly set the background to `IosTheme.color(.background, ...)` to override system defaults.

## Verification Plan

### Manual Verification
1. **iPhone Launch**: App starts in the Detail view (Home). Swiping or tapping the sidebar icon transitions to the Sidebar view.
2. **iPad Launch**: App starts in Detail view. Sidebar can be toggled via the single system-provided button.
3. **Theme Check**: The sidebar and all its elements (toggles, text) respect the dark theme correctly.
4. **Visual Polish**: Scrolling content shows a smooth, translucent blur in the top bar.
