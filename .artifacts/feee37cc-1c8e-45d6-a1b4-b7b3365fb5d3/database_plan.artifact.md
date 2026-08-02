# 🗄️ Стратегический план Базы Данных (Server-Side)

Концепция: Минималистичная реляционная структура + гибкие JSON-контейнеры для метаданных и AI-контекста.

## 1. Сущности и Таблицы

### 👤 UserTable (Директор)
*Центральный профиль владельца расследований.*
*   `id` (UUID)
*   `firebase_uid` (String) — Связь с Auth.
*   `additional_data` (JSONB) — **Портрет Директора**:
    ```json
    {
      "investigator_profile": {
        "device_model": "iPhone 15",
        "language": "ru",
        "region": "UA",
        "timezone": "Europe/Kyiv",
        "app_version": "1.0.0"
      },
      "preferences": {
        "preferred_detective_id": "standard_pro"
      }
    }
    ```

### 📂 SubjectTable (Папка Дела)
*Основной контейнер расследования. Субъект = Дело.*
*   `id` (UUID)
*   `owner_id` (Reference -> UserTable)
*   `name` (String) — Default: Undefined-N.
*   `avatar` (String) — Stores either Emoji (default) or URI/URL of uploaded image.
*   `is_default_avatar` (Boolean) — To distinguish between emoji rendering and image loading.
*   `personality_config` (JSONB) — Suspect characteristics.
*   `detective_settings` (JSONB) — **Настройки Детектива**:
    ```json
    {
      "persona_id": "aggressive_interrogator",
      "focus_areas": ["emotions", "logic_consistency"],
      "analysis_depth": "deep"
    }
    ```

### 🎙️ RecordingTable (Золотой Актив)
*Хранение математических слепков голоса.*
*   `id` (UUID)
*   `subject_id` (Reference -> SubjectTable)
*   `storage_path` (String) — Путь в Firebase Storage.
*   `acoustic_fingerprint` (JSONB) — **Слепок**: Математический граф для обучения и повторного анализа без аудио.
*   `ai_metadata` (JSONB) — Транскрипция, интонационные теги.

### 🖼️ EvidenceTable (Прочие Улики)
*Универсальная таблица для фото, скриншотов и заметок.*
*   `id` (UUID)
*   `subject_id` (Reference -> SubjectTable)
*   `type` (Enum: IMAGE, TEXT, DOCUMENT)
*   `storage_path` (String) — Для фото/доков.
*   `content_raw` (Text) — Для текстовых заметок.
*   `metadata` (JSONB) — Результаты OCR, описание, теги.

### 🧠 AnalysisTable (Вердикты)
*Результаты работы ИИ.*
*   `id` (UUID)
*   `subject_id` (Reference -> SubjectTable)
*   `materials_used` (JSONB) — Список ID из `RecordingTable` и `EvidenceTable`.
*   `system_prompt_id` (Reference -> SystemPromptTable) — Какой "Детектив" делал анализ.
*   `verdict_data` (JSONB) — Оценки, обоснование, инсайты.

### 📜 SystemPromptTable (Инструкции Детектива)
*Слой управления поведением ИИ.*
*   `id` (String) — Уникальный ID промпта (например, `interrogator_v2`).
*   `role_name` (String)
*   `instructions` (Text) — Системная инструкция для LLM.
*   `min_subscription_tier` (String) — Доступность (Free, Pro).

---

## 💎 Почему это «Бессмертно»:
1.  **Acoustic Fingerprint**: Даже если аудио удалено, мы владеем данными для обучения своей модели.
2.  **Multiverse of Prompts**: Мы можем бесконечно добавлять новых "Детективов" (системные промпты) без изменения кода приложений.
3.  **Cross-Material Analysis**: Структура позволяет ИИ "связывать" голос из аудио и факты из скриншота переписки в один отчет.
