# Refactoring API Registration and Helper Functions

The goal is to unify the API registration style using Ktor extension functions and introduce a common helper for extracting authenticated user principals.

## User Review Required

> [!IMPORTANT]
> The `UserApi`, `SubjectApi`, and `AnalysisApi` classes will be changed to use extension functions on `Route`. This changes how they are registered in `Application.kt`.

## Proposed Changes

### [Server API Component]

#### [MODIFY] [BaseApi.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/server/src/main/kotlin/application/liedetector/api/BaseApi.kt)
- Fix imports.
- Add `requirePrincipal()` extension function.

#### [MODIFY] [UserApi.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/server/src/main/kotlin/application/liedetector/api/UserApi.kt)
- Replace `register` method with `Route.userApi` extension function.
- Use `requirePrincipal()`.

#### [MODIFY] [SubjectApi.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/server/src/main/kotlin/application/liedetector/api/SubjectApi.kt)
- Replace `register` method with `Route.subjectApi` extension function.
- Use `requirePrincipal()`.

#### [MODIFY] [AnalysisApi.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/server/src/main/kotlin/application/liedetector/api/AnalysisApi.kt)
- Replace `register` method with `Route.analysisApi` extension function.
- Use `requirePrincipal()`.

#### [MODIFY] [Application.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/server/src/main/kotlin/application/liedetector/Application.kt)
- Update `routing` block to use the new extension functions.

## Verification Plan

### Automated Tests
- Build the project to ensure no compilation errors.
- Run existing tests (if any) to verify API functionality.

### Manual Verification
- Deploy the server locally and verify that the `/api/v1/status` and other endpoints still respond correctly.
