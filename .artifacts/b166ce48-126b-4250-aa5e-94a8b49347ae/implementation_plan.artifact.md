# Fix Subject Subscription and Cache Reactivity Issue

The issue where subjects sometimes don't appear on the main page until a full app restart is likely caused by a bug in the `CacheRepositoryImpl`. The repository's data stream suppresses updates when the raw JSON response in the database hasn't changed, even if its expiration timestamp has been refreshed. This prevents the UI from seeing that previously expired data is now valid.

## Proposed Changes

### Database Engine

#### [MODIFY] [CacheRepositoryImpl.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/engine/src/commonMain/kotlin/application/liedetector/engine/database/internal/CacheRepositoryImpl.kt)

- Refactor `getFlow` to move `distinctUntilChanged()` after the expiration check and JSON decoding.
- This ensures that if data goes from "expired" (null) to "valid" (decoded data), the flow will correctly emit the new state, even if the underlying JSON string remained identical.

### Shared Logic (Optional but recommended for debugging)

#### [MODIFY] [MainViewModel.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/sharedLogic/src/commonMain/kotlin/application/liedetector/presentation/main/MainViewModel.kt)

- Add debug logging in `updateStateWithSubjects` to track when and what data is being received from the repository.

## Verification Plan

### Manual Verification
1. Launch the app with some subjects already in the local database but with an expired timestamp (can be simulated or just wait).
2. Observe that the main screen initially shows the "Welcome" state (empty).
3. Trigger a sync (should happen automatically on `init`).
4. Verify that the subjects appear on the screen immediately after the sync finishes, without requiring an app restart.
5. Verify that subsequent "refreshes" (calling `loadContent`) don't cause unnecessary UI flickers if the data hasn't changed.
