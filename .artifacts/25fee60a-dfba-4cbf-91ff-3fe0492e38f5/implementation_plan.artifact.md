# Fix Analyzer Widget Logic and State Management

Adjust the behavior of the analyzer widget to be "ironclad" per requirements: auto-start for new sessions, resume from drafts without auto-starting, disable controls/dismissal during active recording, and fix the "toggling" bug of the main button.

## User Review Required

> [!IMPORTANT]
> The "Analyze" button on the main screen will no longer toggle the widget closed if it is already open. Instead, it will always ensure the widget is visible. To close the widget, the user must either swipe it down (when paused) or use the Save/Delete buttons.

## Proposed Changes

### 1. [Component Name] Shared Logic (Presentation Layer)

#### [MODIFY] [MainViewModel.kt](file:///Users/krampus/AndroidStudioProjects/KMP/Poligraf/app/sharedLogic/src/commonMain/kotlin/application/poligraf/presentation/main/MainViewModel.kt)
- Update `onWidgetAction` for `RecordingAction.StartNew` to call `openAnalyzer(true)` instead of toggling.
- Ensure that if recording is in progress, the button just brings the widget back to focus.

#### [MODIFY] [AnalyzerViewModel.kt](file:///Users/krampus/AndroidStudioProjects/KMP/Poligraf/app/sharedLogic/src/commonMain/kotlin/application/poligraf/presentation/main/AnalyzerViewModel.kt)
- Refine `onAppear` logic:
  - If a draft exists, just resume the state in pause mode (don't start capture).
  - If it's a completely new session (no draft, no recording in progress), start recording automatically.

---

### 2. [Component Name] Data Layer (Repository)

#### [MODIFY] [AnalyzerRepositoryImpl.kt](file:///Users/krampus/AndroidStudioProjects/KMP/Poligraf/app/data/src/commonMain/kotlin/application/poligraf/data/repository/AnalyzerRepositoryImpl.kt)
- Ensure `resumeFromDraft` doesn't accidentally trigger capture.
- Ensure `startAnalysis` correctly initializes a fresh state.

---

### 3. [Component Name] UI Layer

#### [MODIFY] [MainBottomSheet.kt](file:///Users/krampus/AndroidStudioProjects/KMP/Poligraf/app/sharedLogic/src/commonMain/kotlin/application/poligraf/presentation/main/ui/MainBottomSheet.kt)
- Tighten the `confirmValueChange` logic to ensure that an active recording (not paused) absolutely prevents dismissal.

#### [MODIFY] [AnalyzerDisplay.kt](file:///Users/krampus/AndroidStudioProjects/KMP/Poligraf/app/ui/src/commonMain/kotlin/application/poligraf/ui/features/recorder/AnalyzerDisplay.kt)
- Update `AnalyzerHeader` to strictly disable Save/Delete buttons when recording is active (`!isPaused`).
- Update `AnalyzerControls` to show correct icons and handle transitions between Start/Pause/Resume.

## Verification Plan

### Manual Verification
1. **Fresh Start:**
   - Open app. Click "Analyze".
   - Verify widget opens and recording starts automatically.
   - Verify Save/Delete are disabled and swiping down is impossible.
2. **Pause & Minimize:**
   - Press "Pause".
   - Verify Save/Delete are enabled.
   - Swipe down to minimize.
   - Click "Analyze" again. Verify widget reopens in paused state.
3. **App Restart with Draft:**
   - Start recording. Kill the app.
   - Reopen app.
   - Verify widget opens automatically in paused state with previous duration.
   - Verify recording does NOT start until "Play" is pressed.
4. **Save/Delete:**
   - Verify that clicking "Save" or "Delete" stops the recording and closes the widget.
