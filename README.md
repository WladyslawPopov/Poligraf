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

### Conversational Live Guidance Engine & Dual-Stream Architecture
Poligraf separates raw physical acoustic data into two synchronized streams:
1.  **Live 20 FPS Visualizations (50ms Step):** Every 50ms atom streams live `jitterScore`, `pitchScore`, and `rmsScore` metrics to all UI skins (`Voice Ribbon`, `Equalizer`, `State Map`, `Rings`) and metric bars, producing smooth, analog-like live feedback.
2.  **Quantum Window Headline Text Status ($T \in [1.0s, 3.0s]$):** Headline text statuses are calculated by aggregating voice metrics over a user-configurable Quantum Window. Status phrases change smoothly once per quantum window, eliminating 50ms text flickering or jumping.

### Prioritized Conversational State Matrix
1.  **Priority 1 (Warmup Profiling):** First 5 seconds (`timestamp < 5000ms`) displays `Calibrating to speaker's voice...` while the adaptive noise floor and baseline accumulate initial speech statistics.
2.  **Priority 2 (Hardware Warnings):** Immediate alerts for `Microphone clipping` ($RMS > 0.85$) or `Low signal level`.
3.  **Priority 3 (Acute Conversation States):** Aggregated over the quantum window:
    *   `Strong internal tension` (Jitter + Pitch)
    *   `Tone is more irritated than usual` (Jitter + RMS)
    *   `Active argument & sharp tone` (Pitch + RMS)
    *   `Tense & volatile situation` (Jitter + Pitch + RMS)
    *   `Nervousness in tone` (Jitter spike)
    *   `Increasing tension in dialogue` (Pitch spike)
    *   `Tone is louder with pressure` (RMS spike)
4.  **Priority 4 (Slight Inflections):** Displays `Slight emotional inflections` when Z-scores exceed glow threshold ($1.1\sigma / 0.22f$).
5.  **Priority 5 (Calm Conversation):** Baseline state displaying `Conversation is going calmly` when speech is within baseline norms or during pauses.

### Interpretation Matrix
| Combination | Acoustic Pattern | Conversational Interpretation | Key Focus |
| :--- | :--- | :--- | :--- |
| **Jitter only** | Micro-tremor | `Nervousness in tone` | Watch duration, not single spikes |
| **Pitch only** | Frequency jump | `Increasing tension in dialogue` | Series of jumps is significant |
| **RMS only** | Energy increase | `Tone is louder with pressure` | Forceful delivery, vocal emphasis |
| **Jitter + Pitch** | Micro-tremor + Pitch jump | `Strong internal tension` | Internal anxiety masked externally |
| **Jitter + RMS** | Micro-tremor + High volume | `Tone is more irritated than usual` | Compensatory irritation/bravado |
| **Pitch + RMS** | High volume + Pitch jump | `Active argument & sharp tone` | Conscious pressure / argument |
| **Jitter + Pitch + RMS** | All three active | `Tense & volatile situation` | Highest priority agitation marker |
| *None / Silence* | Baseline levels | `Conversation is going calmly` | Reference level for comparison |

---

## ⚡ Science-Grade DSP Core & Reactive Preferences

*   **Dual-Stream Reactive Architecture:** Settings changes (Quantum Window duration $1.0s \dots 3.0s$ and Sensitivity level `Low` / `Medium` / `High`) are stored in `PreferencesRepository` via reactive `StateFlow`s and applied synchronously on the next 50ms frame without restarting recording sessions.
*   **Dynamic Delta Sigma Sensitivity ($\Delta\sigma$):** Scales anomaly detection thresholds dynamically:
    *   **Low:** $\Delta\sigma = 2.86$ ($1.30 \times \text{ANOMALY\_SIGMA}$) — higher bar for quiet rooms.
    *   **Medium:** $\Delta\sigma = 2.20$ ($1.00 \times \text{ANOMALY\_SIGMA}$) — standard baseline norm.
    *   **High:** $\Delta\sigma = 1.76$ ($0.80 \times \text{ANOMALY\_SIGMA}$) — captures micro-fluctuations.
*   **Parabolic Sub-Sample Pitch Interpolation (0.01 Hz Precision):** Fits a parabola through autocorrelation peaks to increase pitch resolution from ~1 Hz to **0.01 Hz**, eliminating integer lag quantization noise.
*   **High-Frequency Energy Ratio Gating ($hfRatio$):** Evaluates sample-to-sample derivative energy ratios ($hfRatio < 0.00012f$) to filter out sub-bass hand movement (< 30 Hz) while passing 100% of human vocal fundamentals ($85\text{–}400\text{ Hz}$).
*   **Median-Based RMS Shouting Protection:** Uses the 50th percentile (median) of speech RMS for baseline profiling, preventing sudden shouting outbursts from corrupting baseline norms.
*   **Hardware AGC Bypass & Silence Fallback:** `AndroidAudioRecorder` prioritizes `AudioSource.VOICE_RECOGNITION` and `UNPROCESSED` (with fallback to `MIC`) with silence detection.
*   **Non-linear Intensity Mapping ($x^{0.60}$):** Power curve transfer function for UI visualizations amplifies subtle physiological tremors ($0.05 \dots 0.30$ range).
*   **Typewriter UI Engine:** `TypingText` composable renders status phrase changes letter-by-letter (25ms delay) with ghost space reservation to prevent layout shifts.

---

## 🛠️ Execution Lifecycle Guide

```
[Hardware Mic] ──> [AndroidAudioRecorder (PCM 16-bit Mono @ 44.1kHz)]
                         │
                         ▼
        [100ms Windows w/ 50ms Overlap (20 FPS)]
                         │
                         ▼
             [AudioAnalyzer.processAtom]
    ├── Parabolic Peak 0.01Hz + hfRatio Gating
    ├── calculateRms
    └── calculateJitter (Micro-tremors)
                         │
                         ▼
          [AudioAnalyzer.calculateHonestAnalysis]
    ├── Dual-Track Baseline (MovingBaseline + GlobalProfile)
    ├── Dynamic Delta Sigma Z-scores (Sensitivity Scaling)
    └── Look-ahead Context Verification (600ms future buffer)
                         │
                         ▼
         ┌───────────────┴───────────────┐
         ▼                               ▼
[20 FPS Live Metrics Stream]    [Quantum Window Aggregator]
 (Jitter, Pitch, RMS -> UI)      (Averages over 1.0s..3.0s window)
         │                               │
         ▼                               ▼
[Emitted to currentFrame]       [Updates currentQuantumStatus]
 (Real-time Equalizer/Rings)     (Headline Text Status - Stable)
         │                               │
         └───────────────┬───────────────┘
                         │
                         ▼
             [Batch Write to SQLite]
```

### 1. Live Recording Phase (`startAnalysis`)
*   **Hardware Capture:** `AndroidAudioRecorder` captures raw PCM 16-bit mono audio at 44.1 kHz.
*   **Atomization:** Audio is split into 100ms windows with 50% overlap (50ms step / 20 FPS).
*   **Real-time Processing (`processAtom`):** Pitch, RMS, and Jitter are computed. Active speech atoms update `MovingBaseline` and temporary `CalibrationFrame` DB table.
*   **Dual-Stream Finalization (`finalizeFrame`):**
    *   Emits 20 FPS live `AudioFrame` metrics to `_currentFrame` and `_audioFrames` for smooth chart and gauge rendering.
    *   Accumulates sub-frames into `quantumSubFrames`.
    *   Every $N$ seconds (Quantum Window duration setting), `flushQuantumFrame()` calculates average window metrics and updates `currentQuantumStatus`.
*   **Asynchronous Persistence:** Every 5 seconds (100 frames), `persistFrames` writes `AudioFrame` batch to SQLite DB.
*   **UI Rendering:** `AnalyzerSessionController` ingests `AudioFrame`, updates live gauges at 20 FPS, and renders the stable headline status.

### 2. Pause & Seek Phase (`pauseAnalysis` / `onSeek`)
*   Audio capture job is cancelled.
*   Look-ahead queue atoms are flushed to SQLite DB.
*   Timeline seeking retrieves closest frame by timestamp using binary search in $O(\log N)$ time.

### 3. Save Phase (`stopAnalysis(save = true)`)
*   **Zero Re-Analysis (Instant 0.002s Save):** All frames in SQLite `SessionFrame` are clean and calibrated.
*   **Cleanup:** Temporary `CalibrationFrame` table is deleted (`deleteCalibrationData`).
*   **Indexed Anomaly Count:** `countAnomaliesBySessionId` performs an indexed SQL count in 0.001s.
*   **Finalization:** `SessionEntity` updated with `isCompleted = 1`, `duration`, and `anomalyCount`.

### 4. Review Mode Phase (History Detail)
*   `repository.getFramesForSession(sessionId)` loads stored `AudioFrame`s from SQLite DB.
*   Filters initial 5s warmup phase (`timestamp >= 5000ms`).
*   Loads frames into `AnalyzerSessionController` for 1:1 fidelity review.

---

## 🏛️ Feature-First Clean Architecture

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
│   │   │   ├── MarkerShape.kt
│   │   │   ├── QuantumWindowDuration.kt
│   │   │   └── SensitivityLevel.kt
│   │   ├── model/             # 📦 Data classes ONLY
│   │   │   └── AudioFrame.kt  # Clean 7-field UI Frame
│   │   └── repository/
│   │       └── AnalyzerRepository.kt
│   ├── history/
│   │   └── repository/HistoryRepository.kt
│   └── preferences/
│       └── repository/PreferencesRepository.kt
│
├── data/                      # 🗄️ Data Layer (Internal implementations)
│   ├── analyzer/
│   │   ├── dsp/               # Internal DSP math & algorithms
│   │   │   ├── AudioAnalyzer.kt
│   │   │   ├── MovingBaseline.kt
│   │   │   ├── AnalyzerThresholds.kt
│   │   │   ├── AnalysisStatusResolver.kt
│   │   │   └── AnalyzerProcessor.kt
│   │   └── AnalyzerRepositoryImpl.kt # Internal repository implementation
│   ├── history/HistoryRepositoryImpl.kt
│   ├── preferences/PreferencesRepositoryImpl.kt
│   └── di/DataModule.kt
│
├── sharedLogic/               # 🧠 Presentation Layer (ViewModels & Logic)
│   └── presentation/
│       ├── analyzer/
│       │   ├── logic/
│       │   │   ├── AnalyzerSessionController.kt
│       │   │   ├── AnalyzerUiMapper.kt
│       │   │   └── AnalyzerDisplaySnapshot.kt
│       │   └── AnalyzerViewModel.kt
│       └── main/
│           ├── MainViewModel.kt
│           └── ui/SettingsContent.kt
│
└── ui/                        # 🎨 UI Layer (Compose Multiplatform & Design System)
    ├── components/
    ├── features/settings/
    │   ├── QuantumWindowSelectionItem.kt
    │   ├── SensitivitySelectionItem.kt
    │   ├── SkinSelectionItem.kt
    │   └── ShapeSelectionItem.kt
    └── theme/
```

---

## 💎 The Clean `AudioFrame` Domain Model

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

## 10. Technological Boundary & Physical Limits

This on-device Kotlin Multiplatform DSP engine represents the **absolute mathematical, physical, and signal-processing maximum** that can be extracted directly from physical sound wave acoustics on a mobile instrument.

Anything beyond this deterministic acoustic core — such as attempting cloud machine learning, server-side neural network emotion guessing, or artificial "truth/lie" probability scores — strays into non-scientific, non-deterministic toy slop that compromises the mathematical integrity of a professional physical instrument.
