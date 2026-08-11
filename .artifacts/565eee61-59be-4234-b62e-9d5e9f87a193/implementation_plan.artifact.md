# План по устранению блокировки основного потока в диктофоне на iOS

Причина проблемы: методы `stop()` и `trim()` в `AudioRecorder` являются синхронными. В iOS-реализации (`NativeVoiceRecorderEngine.swift`) эти методы используют `DispatchSemaphore` для ожидания завершения асинхронных операций (склейка и обрезка аудио через `AVAssetExportSession`). Поскольку эти методы вызываются из ViewModel (в основном потоке), это приводит к "замиранию" интерфейса на несколько секунд.

## User Review Required

> [!IMPORTANT]
> Изменение интерфейса `AudioRecorder` затронет обе платформы (Android и iOS). Методы `stop()` и `trim()` станут `suspend`, что потребует обновления всех мест их вызова в ViewModel.

## Proposed Changes

### 1. Common Logic (Shared Logic)

#### [MODIFY] [AudioRecorder.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/engine/src/commonMain/kotlin/application/liedetector/engine/io/audio/AudioRecorder.kt)
- Сделать методы `stop()` и `trim()` suspend-функциями.

---

### 2. Android Implementation

#### [MODIFY] [AndroidAudioRecorder.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/engine/src/androidMain/kotlin/application/liedetector/engine/io/audio/AndroidAudioRecorder.kt)
- Обновить реализацию `stop()` и `trim()`, добавив `suspend` и обернув тяжелые операции в `withContext(Dispatchers.IO)`. Это предотвратит потенциальные (хоть и менее заметные) лаги на Android при работе с файлами.

---

### 3. iOS Implementation (KMP Bridge)

#### [MODIFY] [IosAudioRecorder.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/engine/src/nativeMain/kotlin/application/liedetector/engine/io/audio/IosAudioRecorder.kt)
- Обновить интерфейс `Delegate`, сделав методы `stop` и `trim` suspend-функциями.
- Обновить саму реализацию `IosAudioRecorder` для поддержки этих изменений.

---

### 4. iOS Implementation (Native Swift Engine)

#### [MODIFY] [NativeVoiceRecorderEngine.swift](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/iosApp/iosApp/device/audio/NativeVoiceRecorderEngine.swift)
- Удалить использование `DispatchSemaphore`.
- Переписать методы `stop()` и `trim()` на использование `async/await` и `withCheckedContinuation`.
- Оптимизировать инициализацию `AVAudioPlayer` в методе `play()`, чтобы она не влияла на отзывчивость.

---

### 5. Presentation Layer

#### [MODIFY] [RecordingsHistoryViewModel.kt](file:///Users/krampus/AndroidStudioProjects/KMP/LieDetector/app/sharedLogic/src/commonMain/kotlin/application/liedetector/presentation/recordingHistory/RecordingsHistoryViewModel.kt)
- Обновить методы `stopRecording`, `onPlayClicked`, `onSaveClicked`, `onRecordingClicked` и `onTrim`, чтобы они корректно вызывали новые suspend-методы внутри корутин.
- Гарантировать, что `play()` вызывается только после успешного завершения `stop()`.

## Verification Plan

### Manual Verification
1. **Запись на iOS**: Начать запись, поставить на паузу, нажать "Play". Убедиться, что интерфейс не замирает и воспроизведение начинается плавно.
2. **Сохранение**: Нажать "Save" после длинной записи. Убедиться, что индикатор загрузки (`isLoading`) работает, а UI остается отзывчивым во время склейки.
3. **Обрезка (Trim)**: Проверить работу функции обрезки, убедиться в отсутствии фризов.
4. **Android**: Проверить, что запись и воспроизведение на Android продолжают работать корректно.
