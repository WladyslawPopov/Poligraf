# Implementation Plan: Smooth Typing Animation (Layout Stability)

This plan fixes the "jittery" typing animation where text jumps lines or shifts layout as characters are added. We will use the "Ghost Text" technique to pre-calculate and reserve the final layout space.

## User Review Required

> [!IMPORTANT]
> The animation will no longer cause layout shifts. We will reserve the full space of the text message using an invisible layer, ensuring the container size is constant from the first character typed.

## Proposed Changes

### 1. Android App (Native UI)

#### [MODIFY] [WelcomeTextRenderer.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/androidApp/src/main/kotlin/application/liedetector/ui/components/widgets/WelcomeTextRenderer.kt)
- Update `TypingText` to use a `Box` layout.
- Add an invisible `Text` layer containing the **full string** to reserve the height and width.
- Overlay the **animated string** `Text` on top.

---

### 2. iOS App (Native UI)

#### [MODIFY] [WelcomeTextView.swift](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/iosApp/iosApp/UI/Widgets/WelcomeTextView.swift)
- Update `TypingTextView` to use a `ZStack`.
- Add an invisible `Text` layer (`.opacity(0)`) containing the **full string** to prevent line-jumping.
- Overlay the **animated string** `Text` on top.

---

## Verification Plan

### Manual Verification
- **Visual Check (iOS & Android)**: Watch the "Welcome to Lie Detector 🤖" animation.
    - Confirm the container doesn't "jump" or expand as text wraps to the second line.
    - Confirm emojis are rendered correctly as a single unit (no intermediate "broken" character states).
- **Parallax Consistency**: Ensure the background remains visible and smooth during the text animation.
