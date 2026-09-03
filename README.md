# Poligraf (Free Core) 🎙️

> **It’s not a magic button. It’s a professional instrument.**

**Poligraf** is a cross-platform (Kotlin Multiplatform / KMP) acoustic analyzer designed to measure the correlates of emotional state in real time. Unlike typical "lie detector" entertainment apps, Poligraf is engineered as a **scientific instrument**. It provides honest, statistical voice stress analysis data, leaving the role of expert interpreter to the human user.

---

## 👁️ Product Concept & Philosophy

### From "Oracle" to "Instrument"
Entertainment apps promise to "detect lies," which is scientifically unfounded and leads to user frustration.
**Our Pivot:** Poligraf does not issue verdicts or judgements. It measures high-precision acoustic correlates (Jitter, Pitch, RMS Volume) and presents them through a strict, industrial, professional aesthetic inspired by laboratory sound meters and polygraph charts.

### What It Measures
*   **Jitter** — Micro-tremors of the vocal cords (correlates of autonomic uncertainty, anxiety, or fear).
*   **Pitch ($F_0$)** — Fluctuations in fundamental vocal frequency (cognitive stress, throat muscle spasms, word-searching).
*   **RMS** — Acoustic amplitude and power (markers of vocal force, dominance, or acoustic pressure).

### Continuous Live Guidance Engine
The headline text overlay never stays blank. It evaluates a prioritized state decision matrix in real time:
1.  **Priority 1 (Warmup Profiling):** First 5 seconds (`timestamp < 5000ms`) displays `Voice profiling in progress` while the adaptive noise floor and baseline accumulate initial speech statistics.
2.  **Priority 2 (Hardware Warnings):** Immediate alerts for `Microphone clipping` ($RMS > 0.85$) or `Low signal level`.
3.  **Priority 3 (Acute Anomalies):** Sticky 2.5s timer for acute stress combinations (`Hidden panic reaction`, `Aggressive speech tone`, `Cognitive stress spike`, `Micro-tremor detected`, etc.).
4.  **Priority 4 (Mild Fluctuation):** Displays `Subtle emotional fluctuation` when Z-scores exceed glow threshold ($1.3\sigma$).
5.  **Priority 5 (Calm Speech):** Baseline state displaying `Speech is steady` when no stress deviation is present.

### Interpretation Matrix
| Combination | Acoustic Pattern | Psychological Interpretation | Key Focus |
| :--- | :--- | :--- | :--- |
| **Jitter only** | Micro-tremor | Background uncertainty, mild anxiety | Watch duration, not single spikes |
| **Pitch only** | Frequency jump | Acute cognitive stress, word-searching | Series of jumps is significant |
| **RMS only** | Energy increase | Forceful delivery, dominance without tension | May be speech style |
| **Jitter + Pitch** | Micro-tremor + Pitch jump | **Hidden Panic** (internal tension masked externally) | Critical subtle combination |
| **Jitter + RMS** | Micro-tremor + High volume | **Compensatory Aggression** (defensive bravado) | Protective defense, not proactive attack |
| **Pitch + RMS** | High volume + Pitch jump | **Confrontational Tone** (controlled conscious pressure) | Direct aggression/irritation |
| **Jitter + Pitch + RMS** | All three active | **Disorganized Pitch State** (emotional agitation) | Highest priority anomaly marker |
| *None* | Baseline levels | **Speech is steady** | Reference level for comparison |

---

## ⚡ Science-Grade DSP Core

*   **Parabolic Sub-Sample Pitch Interpolation (0.01 Hz Precision):** Fits a parabola through the autocorrelation peak to increase pitch resolution from ~1 Hz to **0.01 Hz**, eliminating integer lag quantization noise and improving Jitter accuracy 100x.
*   **High-Frequency Energy Ratio Gating ($hfRatio$):** Evaluates sample-to-sample derivative energy ratios ($hfRatio < 0.00012f$) to filter out sub-bass hand waving (< 30 Hz) and wind turbulence while passing 100% of human vocal fundamentals ($85\text{–}400\text{ Hz}$).
*   **Median-Based RMS Shouting Protection:** Uses the 50th percentile (median) of speech RMS for baseline profiling. This prevents sudden shouting outbursts or loud speech from corrupting the baseline speech norm.
*   **Hardware AGC Bypass:** `AndroidAudioRecorder` prioritizes `AudioSource.VOICE_RECOGNITION` and `UNPROCESSED` (with fallback to `MIC`) to bypass OS Automatic Gain Control and dynamic range compression, preserving raw physical vocal dynamics.
*   **Non-linear Intensity Mapping ($x^{0.60}$):** Uses a power curve transfer function for UI visualizations. This makes subtle, low-range physiological tremors expressive and visible to the human eye without compromising high-end accuracy.
*   **60 FPS Visual Fluidity (Exponential Moving Average):** Integrated EMA smoothing at the data layer ensures that all gauges, rings, and charts move with analog smoothness, even though raw DSP data arrives in discrete 50ms atoms.
*   **Typewriter UI Engine:** Integrated `TypingText` composable renders status phrase changes letter-by-letter (25ms char delay) without quotes or slashes, utilizing a ghost space-reservation layer to prevent layout shifts.

---

## 🛠️ Execution Lifecycle Guide (Where, How, Why & What Runs)

To ensure long-term clarity and zero confusion, here is the complete end-to-end execution lifecycle of a recording session:

```
[Hardware Mic] ──> [AndroidAudioRecorder (PCM 16-bit Mono @ 44.1kHz)]
                         │
                         ▼
        [100ms Windows w/ 50ms Overlap (20 FPS)]
                         │
                         ▼
             [AudioAnalyzer.processAtom]
    ├── estimatePitchWithConfidence (Parabolic Peak 0.01Hz + hfRatio Gating)
    ├── calculateRms
    └── calculateJitter (Micro-tremors)
                         │
                         ▼
          [AudioAnalyzer.calculateHonestAnalysis]
    ├── Dual-Track Baseline (MovingBaseline + GlobalProfile)
    ├── Z-score Calculation (JitterZ, PitchZ, RmsZ)
    ├── Look-ahead Context Verification (600ms future buffer)
    └── EMA Live Smoothing (0.18 Alpha)
                         │
                         ▼
         [Construct Pure Clean AudioFrame (7 Fields)]
                         │
        ┌────────────────┴────────────────┐
        ▼                                 ▼
[Emit to Live UI StateFlow]   [Batch Write to SQLite (SessionFrame)]
```

### 1. Live Recording Phase (`startAnalysis`)
*   **Hardware Proxy:** `AndroidAudioRecorder` captures raw PCM 16-bit mono audio at 44.1 kHz.
*   **Atomization:** Audio is split into 100ms windows with 50% overlap (50ms step / 20 frames per second).
*   **Real-time Processing (`processAtom`):** Pitch, RMS, and Jitter are computed. VAD (Voice Activity Detection) routes active speech atoms to `MovingBaseline` and temporary `CalibrationFrame` DB table.
*   **Finalization (`finalizeFrame`):** `AudioAnalyzer.calculateHonestAnalysis` computes statistical Z-scores and produces a clean 7-field `AudioFrame`.
*   **Asynchronous Persistence:** Every 5 seconds (100 frames), `persistFrames` writes `AudioFrame` batch to SQLite DB in background.
*   **UI Rendering:** `AnalyzerSessionController` ingests `AudioFrame`, updates live gauges, appends 600ms clustered timeline markers, and emits `AnalyzerDisplaySnapshot` to Compose UI.

### 2. Pause & Seek Phase (`pauseAnalysis` / `onSeek`)
*   Audio capture job is cancelled.
*   Remaining look-ahead queue atoms are flushed to SQLite DB.
*   When seeking on timeline, `AnalyzerProcessor.findClosestFrame` retrieves closest frame by timestamp in $O(\log N)$ time using binary search.

### 3. Save Phase (`stopAnalysis(save = true)`)
*   **Zero Re-Analysis (Instant 0.002s Save):** No offline re-analysis or sequential loops. All frames in SQLite `SessionFrame` are ALREADY clean, calibrated, and smoothed.
*   **Cleanup:** Temporary `CalibrationFrame` DB table is deleted (`deleteCalibrationData`). Uncalibrated initial 5s warmup frames are deleted (`deleteUncalibratedFrames`).
*   **Indexed Anomaly Count:** `countAnomaliesBySessionId` performs an indexed SQL count in 0.001s.
*   **Finalization:** `SessionEntity` is updated with `isCompleted = 1`, `duration`, and `anomalyCount`. Seamlessly transitions to Review Mode.

### 4. Review Mode Phase (History Detail)
*   `repository.getFramesForSession(sessionId)` loads the clean saved `AudioFrame`s from SQLite DB.
*   Filters out initial 5s warmup phase (`timestamp >= 5000L`).
*   Loads frames directly into `AnalyzerSessionController`.
*   **1:1 Fidelity:** Renders the exact same charts, gauges, and markers as Live Mode without any secondary re-analysis.

---

## 🏛️ Feature-First Clean Architecture

The project follows a strict **Feature-First Clean Architecture** with **Types/Model Separation**:

```
application.poligraf/
├── domain/                    # 🏛️ Domain Layer (Pure interfaces & models)
│   ├── analyzer/
│   │   ├── types/             # 🏷️ Enums ONLY
│   │   │   ├── AnalysisStatus.kt
│   │   │   ├── SignalLevel.kt
│   │   │   ├── DominantMetric.kt
│   │   │   ├── AnalyzerSkin.kt
│   │   │   ├── AnalyzerMode.kt
│   │   │   └── MarkerShape.kt
│   │   ├── model/             # 📦 Data classes ONLY
│   │   │   └── AudioFrame.kt  # Clean 7-field UI Frame
│   │   └── repository/
│   │       └── AnalyzerRepository.kt
│   ├── history/
│   │   ├── model/
│   │   │   ├── Session.kt
│   │   │   └── SessionNote.kt
│   │   └── repository/
│   │       └── HistoryRepository.kt
│   └── preferences/
│       └── repository/
│           └── PreferencesRepository.kt
│
├── data/                      # 🗄️ Data Layer (Internal implementations)
│   ├── analyzer/
│   │   ├── model/             # Internal data models (RawAtom, AcousticMetrics, GlobalProfile)
│   │   ├── dsp/               # Internal DSP math & algorithms
│   │   │   ├── AudioAnalyzer.kt
│   │   │   ├── MovingBaseline.kt
│   │   │   ├── AnalyzerThresholds.kt
│   │   │   ├── AnalysisStatusResolver.kt
│   │   │   └── AnalyzerProcessor.kt
│   │   └── AnalyzerRepositoryImpl.kt # Internal repository implementation
│   ├── history/
│   │   └── HistoryRepositoryImpl.kt
│   ├── preferences/
│   │   └── PreferencesRepositoryImpl.kt
│   └── di/
│       └── DataModule.kt      # Koin DI module binding internal impls to domain interfaces
│
├── sharedLogic/               # 🧠 Presentation Layer (ViewModels & Logic Mappers)
│   └── presentation/
│       ├── analyzer/
│       │   ├── logic/
│       │   │   ├── AnalyzerSessionController.kt
│       │   │   ├── AnalyzerUiMapper.kt
│       │   │   └── AnalyzerDisplaySnapshot.kt
│       │   └── AnalyzerViewModel.kt
│       └── history/
│           └── HistoryViewModel.kt
│
├── ui/                        # 🎨 UI Layer (Compose Multiplatform & Design System)
│   ├── components/            # Atomic components (TypingText, AppCard, AppIconButton)
│   ├── features/              # Feature screens and overlays
│   └── theme/                 # Design system tokens (ColorToken, DimenToken, StringToken)
│
└── engine/                    # 🎙️ Hardware Engine (Audio capture proxy & SQLDelight)
    ├── io/audio/              # AudioRecorder, AndroidAudioRecorder, IosAudioRecorder
    └── database/              # SQLDelight PoligrafDatabase & migration scripts (1.sqm, 2.sqm, 3.sqm)
```

### Encapsulation Rules
*   All repository implementations (`AnalyzerRepositoryImpl`, `HistoryRepositoryImpl`, `PreferencesRepositoryImpl`) and DSP algorithms are marked **`internal`**.
*   Outside `:app:data`, other modules cannot access repository implementation classes directly. They MUST inject the public domain interfaces (`AnalyzerRepository`, `HistoryRepository`) via Koin DI `dataModule`.
*   Inside every feature package: Enums are strictly in `types/`, Data classes are strictly in `model/`.

---

## 💎 The Clean `AudioFrame` Domain Model

The `AudioFrame` model contains 7 clean, essential fields:

```kotlin
@Serializable
data class AudioFrame(
    val timestamp: Long,                              // Time in ms from session start
    val stressScore: Float,                            // Overall stress score (0..1)
    val jitterScore: Float = 0f,                       // Jitter biomarker score (0..1)
    val pitchScore: Float = 0f,                        // Pitch biomarker score (0..1)
    val rmsScore: Float = 0f,                          // RMS volume biomarker score (0..1)
    val isAnomaly: Boolean = false,                    // True if a timeline marker should appear
    val status: AnalysisStatus = AnalysisStatus.CALM   // Ready-to-display live status
)
```

---

## 🎨 Visual Idioms (Skins)

The app features 4 visualization types, each highlighting different aspects of the vocal state:

1.  **State Map (Barycentric):** The "triangle of states." Visualizes signal combinations as a point on a map (Fear — Stress — Pressure).
2.  **Voice Ribbon:** A dynamic "cardiogram" visualizing voice stress patterns over time.
3.  **Equalizer (VU-Meter):** Studio aesthetic with mirror-growth bars from a central baseline.
4.  **Three Rings:** Activity rings (similar to fitness trackers) displaying metric intensities.

---

## 🔒 Privacy & Ethics

*   **Zero External Storage:** The app does not record, store, or transmit raw voice audio recordings.
*   **Local On-Device Processing:** All processing happens strictly on the local device (On-device KMP DSP).
*   **No Diarization:** We intentionally do not separate voices technically, leaving the correlation of spikes with speakers to the user's expertise.

---

## 🛠️ Build & Development

### Requirements:
*   Android Studio Ladybug+
*   Kotlin 2.0.20+
*   JDK 17
*   macOS + Xcode (for iOS builds)

### Build Commands:
```bash
# Android Build
./gradlew :app:androidApp:assembleDebug

# iOS Build
./gradlew :app:iosApp:assembleDebug
```
