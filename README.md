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

*   **Dual-Track Parallel Calibration:** Continuous background adaptation to ambient room noise (RMS) and vocal characteristics (Pitch & Jitter) with **Outlier Rejection** (stress spikes do not corrupt baseline statistics).
*   **Real-time Timeline Clustering:** Intelligent event quantization (2.5s window) clustering anomalies into clean, readable timeline markers without visual clutter.
*   **Live Context Notes:** Direct note-taking with timestamp and anomaly linkage during live capture and review.

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
