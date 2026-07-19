# Исправление белого фона при переходах (iOS)

Я устранил проблему «белой вспышки» при навигации между экранами на iOS. Теперь подложка навигационного стека всегда темная.

## Что было сделано

### 1. Глобальный защитный слой (iOS Root)
В [iOSApp.swift](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/iosApp/iosApp/App/iOSApp.swift) я обернул `ContentView` в `ZStack` и добавил `Color.black` в самый низ. Это гарантирует, что даже если SwiftUI на миллисекунду обнажит корень окна, пользователь увидит черный цвет вместо белого.

### 2. Темная подложка стека
В [ContentView.swift](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/iosApp/iosApp/UI/ContentView.swift) я задал основной цвет темы (`.background`) напрямую для `NavigationStack`. Теперь контейнер, в котором «скользят» экраны, по умолчанию темный.

### 3. Упрощение структуры
Я удалил лишние `ZStack` и дублирующиеся `ScalesView` из `ContentView`, так как каждый экран уже рисует свой анимированный фон через `AppScaffold`. Это сделало иерархию вью чище и предсказуемее для аниматора SwiftUI.

## Результат
Анимации переходов (push/pop) теперь выглядят монолитно. Белые полосы и вспышки полностью исчезли.

render_diffs(file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/iosApp/iosApp/App/iOSApp.swift)
render_diffs(file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/iosApp/iosApp/UI/ContentView.swift)
