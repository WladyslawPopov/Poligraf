# Design System Standardization & Refactoring

Refactor and standardize the design system (tokens, mappers, and strings) to ensure a consistent, scalable, and maintainable architecture before the project grows further.

## User Review Required

> [!IMPORTANT]
> This refactoring will change almost all UI-related tokens. Existing UI code will need to be updated to match the new token names. I will handle the migration of existing components as part of this plan.

## Proposed Changes

Standardize `DimenToken`, `TypographyToken`, `ColorToken`, and `IAppStrings` into logical categories and scales.

---

### [Component] UI Core Tokens & Mappers

Summary of changes to the core design system components.

#### [MODIFY] [DimenToken.kt](file:///Users/krampus/AndroidStudioProjects/KMP/Poligraf/app/ui-core/src/commonMain/kotlin/application/poligraf/uicore/theme/tokens/DimenToken.kt)
Redefine dimensions into categorical scales:
- `SPACING_...` (TINY, SMALL, MEDIUM, LARGE, XL, XXL)
- `RADIUS_...` (SMALL, MEDIUM, LARGE, XL, FULL)
- `ICON_...` (TINY, SMALL, MEDIUM, LARGE, XL)
- `BUTTON_...` (SMALL, MEDIUM, LARGE)
- `THICKNESS_...` (THIN, MEDIUM, BOLD)
- `SCREEN_...` (MAX_WIDTH)

#### [MODIFY] [TypographyToken.kt](file:///Users/krampus/AndroidStudioProjects/KMP/Poligraf/app/ui-core/src/commonMain/kotlin/application/poligraf/uicore/theme/tokens/TypographyToken.kt)
Adopt Material 3 style naming:
- `DISPLAY_LARGE`, `DISPLAY_MEDIUM`, `DISPLAY_SMALL`
- `HEADLINE_LARGE`, `HEADLINE_MEDIUM`, `HEADLINE_SMALL`
- `TITLE_LARGE`, `TITLE_MEDIUM`, `TITLE_SMALL`
- `BODY_LARGE`, `BODY_MEDIUM`, `BODY_SMALL`
- `LABEL_LARGE`, `LABEL_MEDIUM`, `LABEL_SMALL`

#### [MODIFY] [ColorToken.kt](file:///Users/krampus/AndroidStudioProjects/KMP/Poligraf/app/ui-core/src/commonMain/kotlin/application/poligraf/uicore/theme/tokens/ColorToken.kt)
Reorganize into functional groups:
- `SURFACE_...` (BACKGROUND, PRIMARY, SECONDARY, VARIANT)
- `ACCENT_...` (PRIMARY, SECONDARY, ENERGY)
- `STATE_...` (SUCCESS, ERROR, WARNING)
- `TEXT_...` (PRIMARY, SECONDARY, INVERTED)

#### [MODIFY] [ThemeDefaults.kt](file:///Users/krampus/AndroidStudioProjects/KMP/Poligraf/app/ui-core/src/commonMain/kotlin/application/poligraf/uicore/theme/mappers/ThemeDefaults.kt)
Update the mappers to reflect the new token structures and values.

#### [MODIFY] [IconMapper.kt](file:///Users/krampus/AndroidStudioProjects/KMP/Poligraf/app/ui-core/src/commonMain/kotlin/application/poligraf/uicore/theme/mappers/IconMapper.kt)
Make the mapper `internal` to ensure `DesignSystem` is the single source of truth for icons.

---

### [Component] Localization & Strings

Cleanup and reorganization of the strings interface.

#### [MODIFY] [IAppStrings.kt](file:///Users/krampus/AndroidStudioProjects/KMP/Poligraf/app/ui-core/src/commonMain/kotlin/application/poligraf/uicore/theme/IAppStrings.kt)
- Remove unused strings.
- Consolidate redundant entries.
- Ensure logical grouping (Common, Errors, Recorder, etc.).

---

### [Component] Project-wide Migration

Update all usages of the old tokens across the project.

#### [MODIFY] Multiple Files
I will use the `multi_replace_file_content` tool to update all files identified in the initial research:
- `MainViewModel.kt`, `DebugViewModel.kt`
- `SettingsContent.kt`, `AnalyzeBtnRenderer.kt`, `ScalesBackground.kt`
- `AppScaffold.kt`, `WidgetRenderer.kt`, etc.

## Verification Plan

### Automated Tests
- Run `gradle_sync` to ensure all symbol references are valid.
- Build the project to verify there are no compilation errors.

### Manual Verification
- Deploy to an Android device to verify the UI looks correct with the new tokens.
- Check both Dark and Light modes to ensure color mapping is intact.
