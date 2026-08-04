# Refactoring Main Screen: Stories & Contacts Layout

Adaptive layout for the main screen based on content presence. Transition from large cards (onboarding) to compact "stories" + contact list (daily usage).

## Proposed Changes

### [Design System]
Update tokens to support compact list items and "story" style icons.

#### [MODIFY] [DimenToken.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/ui-core/src/commonMain/kotlin/application/liedetector/uicore/theme/tokens/DimenToken.kt)
- Add `SUBJECT_STORY_SIZE` (size for small icons in the slider).
- Add `SUBJECT_ROW_HEIGHT` (height for list items).
- Add `AVATAR_SIZE_SMALL` (avatar size in the list).

#### [MODIFY] [IconToken.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/ui-core/src/commonMain/kotlin/application/liedetector/uicore/theme/tokens/IconToken.kt)
- Add `DELETE`, `DRAG_HANDLE`, `EDIT`.

#### [MODIFY] [StringToken.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/ui-core/src/commonMain/kotlin/application/liedetector/uicore/theme/tokens/StringToken.kt)
- Add `SECTION_TEMPLATES`, `SECTION_SUBJECTS`.

---

### [UI Core Widgets]
Define new widget types and update existing ones for adaptive behavior.

#### [MODIFY] [UiWidget.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/ui-core/src/commonMain/kotlin/application/liedetector/uicore/widgets/UiWidget.kt)
- Add `SubjectSlider.DisplayMode` (FULL vs STORY).
- Add `SubjectList` widget.
- Update `AppToolbar` to optionally include the welcome text.

---

### [Shared Presentation]
Refactor logic to handle adaptive states and local settings.

#### [MODIFY] [MainViewModel.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/sharedLogic/src/commonMain/kotlin/application/liedetector/presentation/main/MainViewModel.kt)
- Inject `SettingsRepository`.
- Implement `MainState` logic:
    - If `subjects` is empty AND `SettingsRepository.hasBeenWelcomed` is false:
        - Show large `WelcomeText`.
        - Show `SubjectSlider` in `FULL` mode.
    - Else:
        - Update `AppToolbar` with small welcome message.
        - Show `SubjectSlider` in `STORY` mode.
        - Show `SubjectList` with subjects.
- Add selection/deletion logic for `SubjectList`.

---

### [Android UI]
Implement new renderers and update `MainHost`.

#### [MODIFY] [WidgetRenderer.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/androidApp/src/main/kotlin/application/liedetector/ui/components/widgets/WidgetRenderer.kt)
- Add `SubjectList` support.

#### [MODIFY] [SubjectSliderRenderer.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/androidApp/src/main/kotlin/application/liedetector/ui/components/widgets/SubjectSliderRenderer.kt)
- Support `STORY` mode (smaller items, no buttons).

#### [NEW] [SubjectListRenderer.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/androidApp/src/main/kotlin/application/liedetector/ui/components/widgets/SubjectListRenderer.kt)
- Horizontal list items with selection support.

#### [MODIFY] [MainHost.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/androidApp/src/main/kotlin/application/liedetector/ui/screens/main/MainHost.kt)
- Refactor `Scaffold` to handle dynamic toolbar content.

---

### [iOS UI]
Implement new views and update `MainView`.

#### [MODIFY] [WidgetView.swift](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/iosApp/iosApp/UI/Widgets/WidgetView.swift)
- Add `SubjectList` support.

#### [MODIFY] [SubjectSliderView.swift](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/iosApp/iosApp/UI/Widgets/SubjectSliderView.swift)
- Support `STORY` mode.

#### [NEW] [SubjectListView.swift](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/iosApp/iosApp/UI/Widgets/SubjectListView.swift)
- SwiftUI implementation of the contact list.

#### [MODIFY] [MainView.swift](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/iosApp/iosApp/UI/MainView.swift)
- Update toolbar and layout.

## Verification Plan

### Automated Tests
- Unit test `MainViewModel` state transitions.

### Manual Verification
1. Fresh install: Verify large welcome text and big card.
2. Create subject: Verify screen transforms to compact mode (welcome in toolbar, slider becomes small, list appears).
3. Restart app: Verify compact mode is preserved.
