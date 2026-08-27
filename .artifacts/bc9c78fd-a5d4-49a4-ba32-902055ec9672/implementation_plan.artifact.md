# Implementation Plan - History Feature

This plan outlines the implementation of the History feature, focusing on reusability of visualization components and a dedicated session detail view ("Session Report").

## User Review Required

> [!IMPORTANT]
> - `HistoryDetail` will be implemented as a renderer analogous to `AnalyzerRenderer.kt`, reusing existing visualization widgets (`StateMapVisualization`, `VoiceRibbonVisualization`, etc.).
> - History navigation will be integrated into the main flow (as per the "History" action in `MainToolbar`).
> - The session detail view will include new history-specific widgets: Summary, Editable Title, and Notes.

## Proposed Changes

### Data Layer

#### [NEW] [HistoryRepository](file:///Users/krampus/AndroidStudioProjects/KMP/Poligraf/app/data/src/commonMain/kotlin/application/poligraf/domain/repository/HistoryRepository.kt)
Interface for managing saved sessions.
- `getSessions(): Flow<List<SessionEntity>>`
- `getSessionById(id: String): Flow<SessionEntity?>`
- `updateSession(id: String, title: String, notes: String)`
- `deleteSession(id: String)`

#### [NEW] [HistoryRepositoryImpl](file:///Users/krampus/AndroidStudioProjects/KMP/Poligraf/app/data/src/commonMain/kotlin/application/poligraf/data/repository/HistoryRepositoryImpl.kt)
Implementation using `AppDatabase.sq`.

#### [MODIFY] [DataModule](file:///Users/krampus/AndroidStudioProjects/KMP/Poligraf/app/data/src/commonMain/kotlin/application/poligraf/data/di/DataModule.kt)
Register `HistoryRepository` in Koin.

### Presentation Layer (Decompose)

#### [MODIFY] [RootConfig](file:///Users/krampus/AndroidStudioProjects/KMP/Poligraf/app/sharedLogic/src/commonMain/kotlin/application/poligraf/presentation/root/RootConfig.kt)
Add `History` configuration.

#### [MODIFY] [RootComponent](file:///Users/krampus/AndroidStudioProjects/KMP/Poligraf/app/sharedLogic/src/commonMain/kotlin/application/poligraf/presentation/root/RootComponent.kt)
Handle navigation to the History screen.

#### [NEW] [HistoryComponent](file:///Users/krampus/AndroidStudioProjects/KMP/Poligraf/app/sharedLogic/src/commonMain/kotlin/application/poligraf/presentation/history/HistoryComponent.kt)
A Decompose component for the History tab, managing a stack for "List" and "Detail" views.

#### [NEW] [HistoryViewModel](file:///Users/krampus/AndroidStudioProjects/KMP/Poligraf/app/sharedLogic/src/commonMain/kotlin/application/poligraf/presentation/history/HistoryViewModel.kt)
Manages the list of sessions.

#### [NEW] [HistoryDetailViewModel](file:///Users/krampus/AndroidStudioProjects/KMP/Poligraf/app/sharedLogic/src/commonMain/kotlin/application/poligraf/presentation/history/HistoryDetailViewModel.kt)
Manages the state for a specific session.
- Loads frames for visualization.
- Handles title/notes editing.
- Calculates summary (anomaly count, duration, volatility).

### UI Layer (Compose)

#### [NEW] [HistoryContent](file:///Users/krampus/AndroidStudioProjects/KMP/Poligraf/app/sharedLogic/src/commonMain/kotlin/application/poligraf/presentation/history/HistoryContent.kt)
The main entry point for the History tab.

#### [NEW] [HistoryListRenderer](file:///Users/krampus/AndroidStudioProjects/KMP/Poligraf/app/sharedLogic/src/commonMain/kotlin/application/poligraf/presentation/history/ui/HistoryListRenderer.kt)
Displays the scrollable list of sessions.

#### [NEW] [HistoryDetailRenderer](file:///Users/krampus/AndroidStudioProjects/KMP/Poligraf/app/sharedLogic/src/commonMain/kotlin/application/poligraf/presentation/history/ui/HistoryDetailRenderer.kt)
**Analogous to `AnalyzerRenderer.kt`**.
- Reuses: `AnalyzerHeader` (modified for history), `StateMapVisualization`, `VoiceRibbonVisualization`, `EqualizerVisualization`, `RingsVisualization`, `MetricItem`, `AnomalyTimeline`.
- New widgets: `SessionSummaryCard` (Volatility, Marker count, Duration), `NotesField`, `EditableTitle`.
- **Note**: No recording/saving buttons; only playback/seek and save notes.

#### [MODIFY] [MainViewModel](file:///Users/krampus/AndroidStudioProjects/KMP/Poligraf/app/sharedLogic/src/commonMain/kotlin/application/poligraf/presentation/main/MainViewModel.kt)
Trigger navigation to History when `NavigationAction.History` is received.

## Verification Plan

### Automated Tests
- Unit tests for `HistoryRepositoryImpl`.
- State verification in `HistoryDetailViewModel` for summary calculations.

### Manual Verification
1. Record a session and save it.
2. Navigate to History via the toolbar icon.
3. Verify the session appears in the list.
4. Open the session detail.
5. Verify visualization widgets show correct data.
6. Edit the session name and add a note; verify persistence after closing and reopening.
7. Verify "Conclusion" text/color changes based on anomaly count (Green for few, Red for many).
