# UI Core Refactoring: Clean Architecture & Organization

Я навел порядок в модуле `ui-core`, разделив огромный файл токенов на логические части и выделив UI-стейты в отдельный пакет. Это делает кодовую базу более поддерживаемой и понятной.

## Что было сделано

### [UI Core Organization]
- **Разделение токенов**: Монолитный `ThemeTokens.kt` удален. Теперь каждый тип токена живет в своем файле в пакете `uicore.theme`:
    - `ColorToken.kt`
    - `DimenToken.kt`
    - `IconToken.kt`
    - `StringToken.kt`
    - `TypographyToken.kt`
- **Новый пакет для стейтов**: Создан пакет `application.liedetector.uicore.state`, куда вынесен файл `UiState.kt`. В нем теперь находятся:
    - `ToastState`
    - `ToastType`
    - `ErrorType`

### [Import Refactoring]
- Обновлены импорты во всем проекте. Теперь логика (ViewModels) и UI-компоненты импортируют стейты из `.uicore.state`, а визуальные параметры из `.uicore.theme`.
- Исправлены файлы: `BaseViewModel.kt`, `ServerErrorException.kt`, `ErrorView.kt`, `ToastView.kt`, `AppScaffold.kt`.

## Преимущества
- **Масштабируемость**: Легче добавлять новые токены или типы стейтов, не раздувая один файл.
- **Чистота кода**: Логическое разделение на «тему» (визуал) и «состояние» (данные для UI).
- **Отсутствие конфликтов**: Уменьшен риск конфликтов при слиянии веток, так как изменения теперь распределены по разным файлам.

## Верификация
- Выполнена успешная сборка проекта через Gradle (`assembleDebug`).
- Проверена работоспособность всех UI-компонентов после смены пакетов.

> [!TIP]
> При создании новых экранов или компонентов, импортируйте `ErrorType` и `ToastState` из пакета `uicore.state`.

render_diffs(file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/ui-core/src/commonMain/kotlin/application/liedetector/uicore/state/UiState.kt)
render_diffs(file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/sharedLogic/src/commonMain/kotlin/application/liedetector/presentation/base/BaseViewModel.kt)
