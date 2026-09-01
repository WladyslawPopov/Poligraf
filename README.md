# Poligraf (Free Core) 🎙️

> **It’s not a magic button. It’s a professional instrument.**

**Poligraf** is a cross-platform (KMP) acoustic analyzer designed to measure the correlates of emotional state in real-time. Unlike typical "lie detector" entertainment apps, Poligraf is positioned as a **scientific instrument**. It provides honest data, leaving the role of expert and interpreter to the human user.

---

## 👁️ Product Concept

### From "Oracle" to "Instrument"
Most apps in the stores promise to "detect lies," which is scientifically unfounded and leads to user frustration. 
**Our Pivot:** We do not issue verdicts. We provide high-precision acoustic data (Jitter, Pitch, RMS) packaged in a strict, professional aesthetic inspired by industrial sound level meters.

### How It Works
The app analyzes the unified audio stream of the room in passive mode:
*   **Jitter** — Micro-tremors of the vocal cords (markers of uncertainty or fear).
*   **Pitch** — Fluctuations in fundamental frequency (cognitive stress, throat spasms).
*   **RMS** — Amplitude and power (markers of dominance or hidden aggression).

### Interpretation is Human
The app listens to the shared audio stream. It doesn't attempt to technically separate speakers. Like a real polygraph examiner looking at a chart and the subject simultaneously, the user correlates the visual spikes with the conversation context.

### Signal Combinations (Interpretation Matrix)
| Combination | Acoustic Pattern | Psychological Interpretation | Key Focus |
| :--- | :--- | :--- | :--- |
| **Jitter only** | Micro-tremor | Background uncertainty, mild anxiety | Watch duration, not single spikes |
| **Pitch only** | Frequency jump | Acute cognitive stress, word-searching | Series of jumps is significant |
| **RMS only** | Energy increase | Forceful delivery, dominance without tension | May be speech style |
| **Jitter + Pitch** | Micro-tremor + Pitch jump | **Hidden Panic** (internal tension masked externally) | Critical subtle combination |
| **Jitter + RMS** | Micro-tremor + High volume | **Compensatory Aggression** (defensive bravado) | Protective defense, not proactive attack |
| **Pitch + RMS** | High volume + Pitch jump | **Open Confrontation** (controlled conscious pressure) | Direct aggression/irritation |
| **Jitter + Pitch + RMS** | All three active | **Full Disorganization** (emotional outburst, agitation) | Highest priority anomaly marker |
| *None* | Baseline levels | Steady emotional baseline | Reference level for comparison |

---

## ⚡ Core Engine Features

*   **Honest VSA Engine (Scientific Grade):** A high-precision acoustic analyzer that moves beyond simple "equalizer" logic. It uses a three-tier verification system to detect real physiological stress:
    *   **Global Session Profile:** Continuous statistical calibration against the speaker's *entire session history*. It calculates individual norms (90th percentiles, log-domain variance) to establish a true vocal baseline.
    *   **Dynamic Headroom (Adaptive Damper):** Automatically desensitizes the analyzer during loud or aggressive speech segments. It prevents normal emotional accents from being falsely identified as stress by widening the "sigma-norm corridor" in real-time.
    *   **Look-ahead Verification:** Utilizes a 600ms "future" buffer to distinguish between transient autonomic spikes (Stress) and sustained vocal shifts (Adaptation).
*   **Non-linear Intensity Mapping:** Uses a power curve transfer function ($x^{0.6}$) for UI visualizations. This makes subtle, low-range physiological tremors more expressive and visible to the human eye without compromising high-end accuracy.
*   **VAD-Gated Calibration:** Only active speech segments are used for calibration. This eliminates "Silence Bias," where long pauses would artificially lower the baseline.
*   **60 FPS Visual Fluidity:** Integrated Exponential Moving Average (EMA) smoothing at the data layer ensures that all gauges, rings, and charts move with analog-like smoothness, even though raw DSP data arrives in discrete 50ms atoms.
*   **Unified Session Architecture:** Single codepath for both live recording and historical session review. Historical data is "re-lived" through the same Honest Engine (using look-ahead into the stored frames) for 100% data consistency.
*   **DB-First Resilience:** Temporary calibration data is persisted to a local SQLDelight table. If the app crashes or the battery dies, the session can be resumed without losing its learned vocal profile.
*   **High-Resolution Anomaly Timeline:** Zoomed timeline resolution (40dp/s) with 600ms event quantization clustering anomalies into clear, seekable diamond markers.

---

## 🏗️ Architecture (Technical Stack)

The project is built using **Kotlin Multiplatform (KMP)** with a strict separation of concerns based on Clean Architecture principles.

### Module Structure:
*   `:app:sharedLogic` — **The Brain.** Contains `ViewModels`, business logic, and navigation (Decompose). Completely independent of the UI framework.
*   `:app:ui` — **Design System & Features.** Implemented with **Compose Multiplatform**. Contains atomic components, theme tokens, and feature-specific renderers.
*   `:app:domain` — Repository interfaces and pure data models.
*   `:app:data` — Repository implementations, local DB handling (SQLDelight), and Preferences.
*   `:app:engine` — **DSP (Digital Signal Processing).** The mathematical core for PCM stream capture and real-time acoustic metric calculation.

### Architectural Patterns:
*   **Unidirectional Data Flow (UDF):** UI state is managed via `StateFlow` in ViewModels.
*   **AnalyzerProcessor:** A dedicated logic delegate ensuring identical data processing for both "Live" analysis and "History" review.
*   **Design System:** A token-based system (Color, Dimen, Icon, String) allowing for global styling changes from a single source of truth.

---

## 🎨 Visual Idioms (Skins)

The app features 4 visualization types, each highlighting different aspects of the vocal state:

1.  **Equalizer (VU-Meter):** Studio aesthetic with mirror-growth bars from a central baseline. Ideal for assessing instantaneous spikes.
2.  **State Map (Barycentric):** The "triangle of states." Visualizes signal combinations as a point on a map (Fear — Stress — Aggression).
3.  **Voice Ribbon:** A dynamic "cardiogram." Allows viewing voice patterns over time.
4.  **Three Rings:** Activity rings (similar to fitness trackers). The most accessible way to read signal intensity.

---

## 🔒 Privacy & Ethics

*   **Zero Storage:** The app does not record, store, or transmit audio recordings.
*   **Local Only:** All processing happens strictly on the device (On-device DSP). No cloud computing or servers involved.
*   **No Diarization:** We intentionally do not separate voices technically, leaving the correlation of spikes with speakers to the user's expertise.

---

## 🛠️ Development

### Requirements:
*   Android Studio Ladybug+
*   Kotlin 2.0.20+
*   JDK 17
*   macOS + Xcode (for iOS builds)

### Build Commands:
```bash
# Android
./gradlew :app:androidApp:assembleDebug

# iOS
./gradlew :app:iosApp:assembleDebug
```

---
*The project is currently under active development (Free-core MVP).*
