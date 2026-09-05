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

### Conversational Live Guidance Engine & Decoupled Dual-Stream Architecture
Poligraf separates raw physical acoustic data into two completely decoupled, non-interfering data streams:
1.  **Stream 1: Live 20 FPS Acoustic Metrics Stream (50ms Step):** Every 50ms atom streams live `jitterScore`, `pitchScore`, and `rmsScore` metrics to all UI skins (`Voice Ribbon`, `Equalizer`, `State Map`, `Rings`) and metric bars, producing smooth, analog-like live feedback.
2.  **Stream 2: Quantum Window Anomaly & Status Recognition Pipeline ($T \in [1.0s, 3.0s]$):** Headline and secondary status overlays are calculated by aggregating voice metrics over discrete Quantum Window buckets $W_i = [i \cdot T \dots (i+1) \cdot T]$. All 3 text overlay statuses hold 100% solid and constant for the full $T$-second duration before smoothly dissolving into the next window, completely eliminating text flickering.

### Prioritized Conversational State Matrix
1.  **Priority 1 (Warmup Profiling):** First 5 seconds (`timestamp < 5000ms`) displays `Calibrating to speaker's voice...` while the adaptive noise floor and baseline accumulate initial speech statistics in temporary DB storage. Zero timeline markers are emitted during warmup.
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

### Interpretation Matrix & Balanced $\sigma$-Normalized Biomarker Colors
Dominant metrics and timeline marker colors are determined by comparing relative Z-scores ($Z_{\text{jitter}}$, $Z_{\text{pitch}}$, $Z_{\text{rms}}$) in standard deviation ($\sigma$) units relative to learned speaker baselines:

| Combination | Acoustic Pattern | Conversational Interpretation | Dominant Color Token |
| :--- | :--- | :--- | :--- |
| **Jitter only** | Micro-tremor | `Nervousness in tone` | 🟢 **Green** (`CHART_JITTER`) |
| **Pitch only** | Frequency jump | `Increasing tension in dialogue` | 🔵 **Blue** (`CHART_PITCH`) |
| **RMS only** | Energy increase | `Tone is louder with pressure` | 🟠 **Orange** (`CHART_RMS`) |
| **Jitter + Pitch** | Micro-tremor + Pitch jump | `Strong internal tension` | 🟢 / 🔵 Highest $\sigma$-Z-score |
| **Jitter + RMS** | Micro-tremor + High volume | `Tone is more irritated than usual` | 🟢 / 🟠 Highest $\sigma$-Z-score |
| **Pitch + RMS** | High volume + Pitch jump | `Active argument & sharp tone` | 🔵 / 🟠 Highest $\sigma$-Z-score |
| **Jitter + Pitch + RMS** | All three active | `Tense & volatile situation` | Highest $\sigma$-Z-score |
| *None / Silence* | Baseline levels | `Conversation is going calmly` | Reference level |

---

## ⚡ Science-Grade DSP Core & Reactive Preferences

*   **Decoupled Reactive Architecture:** Settings changes (Quantum Window duration $1.0s \dots 3.0s$ and Sensitivity level `Low` / `Medium` / `High`) are stored in `PreferencesRepository` via reactive `StateFlow`s and applied synchronously on the next frame without restarting recording sessions.
*   **Dynamic Delta Sigma Sensitivity ($\Delta\sigma$):** Scales anomaly detection thresholds dynamically:
    *   **Low:** $\Delta\sigma = 2.86$ ($1.30 \times \text{ANOMALY\_SIGMA}$) — higher bar for quiet rooms.
    *   **Medium:** $\Delta\sigma = 2.20$ ($1.00 \times \text{ANOMALY\_SIGMA}$) — standard baseline norm.
    *   **High:** $\Delta\sigma = 1.76$ ($0.80 \times \text{ANOMALY\_SIGMA}$) — captures micro-fluctuations.
*   **Deterministic Quantum Window Marker Indexing (`m_window_$i`):** Quantum Window slots $W_i$ generate markers with deterministic IDs `m_window_${windowIndex}_${status.name}`. Re-evaluation or session finalization flushes update markers in-place, making duplicate or retroactive past-window markers mathematically impossible.
*   **Dedicated SQLite `SessionMarker` Table & Migration `4.sqm`:** SQLDelight schema version 4 persists ready-to-display quantum markers directly during capture. Review Mode loads pre-calculated `SessionMarker` rows directly from SQLite for 1:1 fidelity with zero re-analysis.
*   **3-Tier Status Cascade & Typography:** Primary center headline (85–100% opacity) + top/bottom secondary half-tone texts (100% solid `ColorToken.TEXT_SECONDARY`, `titleMedium` typography, 12dp padding). Smooth 700ms `AnimatedContent` cross-fade dissolve transitions (`LinearOutSlowInEasing`) prevent layout jumps.
*   **3-Parameter History Breakdown & Weighted Review Scenario Scoring:** History list items (`HistoryItem`) display 3 parameters side-by-side: 🔴 **Full Anomalies** (`fullAnomalyCount`), 🟡 **Half-Tone Fluctuations** (`halftoneAnomalyCount`), and 📝 **Notes** (`noteCount`). Review Mode scenario verdicts calculate weighted score $N_{\text{weighted}} = \text{fullCount} \cdot 1.0 + \text{halftoneCount} \cdot 0.5$.
*   **Pure Geometric Rhombus Vector (`AppIcons.GeometricDiamond`):** Clean, symmetrical vector path (`M12,2 L22,12 L12,22 L2,12 Z`) mapped cleanly in the design system theme.
*   **Parabolic Sub-Sample Pitch Interpolation (0.01 Hz Precision):** Fits a parabola through autocorrelation peaks to increase pitch resolution from ~1 Hz to **0.01 Hz**, eliminating integer lag quantization noise.
*   **High-Frequency Energy Ratio Gating ($hfRatio$):** Evaluates sample-to-sample derivative energy ratios ($hfRatio < 0.00012f$) to filter out sub-bass hand movement (< 30 Hz) while passing 100% of human vocal fundamentals ($85\text{–}400\text{ Hz}$).
*   **Median-Based RMS Shouting Protection:** Uses the 50th percentile (median) of speech RMS for baseline profiling, preventing sudden shouting outbursts from corrupting baseline norms.
*   **Hardware AGC Bypass & Silence Fallback:** `AndroidAudioRecorder` prioritizes `AudioSource.VOICE_RECOGNITION` and `UNPROCESSED` (with fallback to `MIC`) with silence detection.
*   **Non-linear Intensity Mapping ($x^{0.60}$):** Power curve transfer function for UI visualizations amplifies subtle physiological tremors ($0.05 \dots 0.30$ range).

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
[Stream 1: Live Metrics Stream] [Stream 2: Quantum Window Pipeline]
 (Jitter, Pitch, RMS -> UI)      (Aggregates over 1.0s..3.0s window)
         │                               │
         ▼                               ▼
[Emitted to currentFrame]       [Updates currentQuantumAnalysis]
 (Real-time Equalizer/Rings)     (3-Tier Text Overlay + SessionMarker DB)
         │                               │
         └───────────────┬───────────────┘
                         │
                         ▼
           [Batch Write to SQLite DB]
```

### 1. Live Recording Phase (`startAnalysis`)
*   **Hardware Capture:** `AndroidAudioRecorder` captures raw PCM 16-bit mono audio at 44.1 kHz.
*   **Atomization:** Audio is split into 100ms windows with 50% overlap (50ms step / 20 FPS).
*   **Real-time Processing (`processAtom`):** Pitch, RMS, and Jitter are computed. Active speech atoms update `MovingBaseline` and temporary `CalibrationFrame` DB table.
*   **Dual-Stream Finalization (`finalizeFrame`):**
    *   **Stream 1:** Emits 20 FPS live `AudioFrame` metrics to `_currentFrame` and `_audioFrames` for smooth chart and gauge rendering.
    *   **Stream 2:** Accumulates sub-frames into `quantumSubFrames`. Every $T$ seconds (Quantum Window duration setting), `flushQuantumFrame()` calculates quantum window analysis, updates `currentQuantumAnalysis`, emits immutable `AnomalyMarker`s, and persists markers to `SessionMarker` DB table.
*   **Asynchronous Persistence:** Every 5 seconds (100 frames), `persistFrames` writes `AudioFrame` batch to SQLite DB.
*   **UI Rendering:** `AnalyzerSessionController` ingests `AudioFrame`, updates live gauges at 20 FPS, and renders 100% solid status overlays.

### 2. Pause & Seek Phase (`pauseAnalysis` / `onSeek`)
*   Audio capture job is cancelled.
*   Look-ahead queue atoms are flushed to SQLite DB.
*   Timeline seeking retrieves closest frame by timestamp using binary search in $O(\log N)$ time.

### 3. Save Phase (`stopAnalysis(save = true)`)
*   **Zero Re-Analysis (Instant 0.002s Save):** All frames in SQLite `SessionFrame` and `SessionMarker` are clean and calibrated.
*   **Cleanup:** Temporary `CalibrationFrame` table is deleted (`deleteCalibrationData`).
*   **Finalization:** `SessionEntity` updated with `isCompleted = 1`, `duration`, and `anomalyCount`.

### 4. Review Mode Phase (History Detail)
*   `repository.getFramesForSession(sessionId)` loads stored `AudioFrame`s from SQLite DB.
*   `repository.getMarkersForSession(sessionId)` loads pre-calculated `SessionMarker` rows directly from SQLite.
*   Filters initial 5s warmup phase (`timestamp >= 5000ms`).
*   Loads frames into `AnalyzerSessionController` for 1:1 fidelity review.
*   Computes weighted review scenario score $N_{\text{weighted}} = \text{fullCount} \cdot 1.0 + \text{halftoneCount} \cdot 0.5$ for accurate volatility and conclusion verdicts.

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
│   │   ├── model/             # 📦 Domain Models
│   │   │   ├── AudioFrame.kt
│   │   │   ├── QuantumAnalysis.kt
│   │   │   └── AnomalyMarker.kt
│   │   └── repository/
│   │       └── AnalyzerRepository.kt
│   ├── history/
│   │   ├── model/Session.kt
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
│   │   │   ├── QuantumWindowAggregator.kt
│   │   │   └── AnalyzerProcessor.kt
│   │   └── AnalyzerRepositoryImpl.kt # Internal repository implementation
│   ├── history/HistoryRepositoryImpl.kt
│   ├── preferences/PreferencesRepositoryImpl.kt
│   └── di/DataModule.kt
│
├── engine/                    # ⚙️ Engine Layer (Database & Driver)
│   └── sqldelight/
│       └── database/
│           ├── AppDatabase.sq
│           └── migrations/4.sqm
│
├── sharedLogic/               # 🧠 Presentation Layer (ViewModels & Logic)
│   └── presentation/
│       ├── analyzer/
│       │   ├── logic/
│       │   │   ├── AnalyzerSessionController.kt
│       │   │   ├── AnalyzerUiMapper.kt
│       │   │   └── AnalyzerDisplaySnapshot.kt
│       │   ├── ui/AnalyzerRenderer.kt
│       │   └── AnalyzerViewModel.kt
│       ├── history/
│       │   ├── ui/HistoryListRenderer.kt
│       │   └── HistoryViewModel.kt
│       └── main/
│           ├── MainViewModel.kt
│           └── ui/SettingsContent.kt
│
└── ui/                        # 🎨 UI Layer (Compose Multiplatform & Design System)
    ├── components/
    ├── features/
    │   ├── analyzer/
    │   │   ├── components/
    │   │   │   ├── AnomalyTimeline.kt
    │   │   │   └── InterpretationOverlay.kt
    │   │   ├── visualizers/
    │   │   │   └── VoiceRibbonVisualization.kt
    │   │   ├── models/AnalyzerMarker.kt
    │   │   └── state/AnalyzerState.kt
    │   ├── history/
    │   │   ├── list/HistoryItem.kt
    │   │   ├── detail/SessionSummaryCard.kt
    │   │   └── state/SessionUiModel.kt
    │   └── settings/
    └── theme/
        ├── AppIcons.kt
        └── mappers/IconMapper.kt
```

---

## 💎 Pure Domain Models

### Clean `AudioFrame` Model
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

### Immutable `AnomalyMarker` Model
```kotlin
@Serializable
data class AnomalyMarker(
    val id: String,                                   // Deterministic ID m_window_$i
    val timestampMillis: Long,                         // Time in ms
    val status: AnalysisStatus,                       // Triggered analysis status
    val dominantMetric: DominantMetric,               // Dominant biomarker (PITCH, JITTER, RMS)
    val isFullAnomaly: Boolean = false,               // True for full anomaly, false for half-tone
    val alpha: Float = 1.0f                           // Opacity value
)
```

---

## 10. Technological Boundary & Physical Limits

This on-device Kotlin Multiplatform DSP engine represents the **absolute mathematical, physical, and signal-processing maximum** that can be extracted directly from physical sound wave acoustics on a mobile instrument.

Anything beyond this deterministic acoustic core — such as attempting cloud machine learning, server-side neural network emotion guessing, or artificial "truth/lie" probability scores — strays into non-scientific, non-deterministic toy slop that compromises the mathematical integrity of a professional physical instrument.
