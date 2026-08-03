# Реструктуризация KMP проекта на слои Engine, Data и Presentation

Цель — создать жесткие архитектурные границы, инкапсулировать DTO внутри слоя данных и обеспечить чистоту ViewModels в `sharedLogic`.

## Proposed Changes

### 1. Инфраструктурный уровень [NEW] `:engine`
Выносим низкоуровневые сервисы (Сеть, БД, Настройки, Аналитика).

#### [NEW] [build.gradle.kts](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/engine/build.gradle.kts)
#### [MOVE] `app/sharedLogic/src/commonMain/kotlin/application/liedetector/engine/*` -> `engine/src/commonMain/kotlin/application/liedetector/engine/*`
#### [MOVE] `app/sharedLogic/src/commonMain/sqldelight/*` -> `engine/src/commonMain/sqldelight/*`

---

### 2. Слой данных и домена [NEW] `:data`
Этот модуль будет единственным, кто видит `:core` (DTO). Он предоставляет чистые Domain модели для UI.

#### [NEW] [build.gradle.kts](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/data/build.gradle.kts)
#### [MOVE] `app/sharedLogic/src/commonMain/kotlin/application/liedetector/data/*` -> `data/src/commonMain/kotlin/application/liedetector/data/*`
#### [NEW] **Domain Models** в `data/src/commonMain/kotlin/application/liedetector/domain/model/`
#### [NEW] **Mappers** в `data/src/commonMain/kotlin/application/liedetector/data/mapper/`

---

### 3. Слой презентации [MODIFY] `:app:sharedLogic`
Очищаем от логики данных. Оставляем ViewModels, Components и Navigation.

#### [MODIFY] [build.gradle.kts](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/sharedLogic/build.gradle.kts)
- Удаляем зависимости Ktor, SQLDelight.
- Добавляем `api(projects.data)`.
- Убираем `api(projects.core)` (теперь DTO недоступны во ViewModel).

---

### 4. Конфигурация проекта
#### [MODIFY] [settings.gradle.kts](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/settings.gradle.kts)
- Добавляем `include(":engine")` и `include(":data")`.

## Verification Plan

### Automated Tests
1. Выполнение `./gradlew :engine:assembleDebug`
2. Выполнение `./gradlew :data:assembleDebug`
3. Выполнение `./gradlew :app:sharedLogic:assembleDebug`

### Manual Verification
1. Проверка того, что `UserDto` больше не доступен для импорта в `RootViewModel`.
2. Проверка инициализации Koin (нужно будет обновить модули).
