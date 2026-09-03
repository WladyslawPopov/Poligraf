# Specification: Scientific Voice Stress Analysis Engine (Poligraf) 🎙️

> **Document Version:** 1.0.0  
> **Date:** September 3, 2026  
> **Target Audience:** Core Developers, DSP Engineers, System Architects  
> **Status:** Production Specification  

---

## 1. System Overview & Core Philosophy

The **Poligraf Analysis Engine** is a cross-platform (Kotlin Multiplatform / KMP) Digital Signal Processing (DSP) system designed to measure the acoustic correlates of physiological and emotional stress in real time.

### 1.1 The "Instrument" Principle
Poligraf is **not an oracle** and does not issue binary "truth/lie" verdicts. In accordance with polygraphy standards, human speech acoustics vary based on context, fatigue, cognitive load, and emotional arousal. Poligraf operates as a **high-precision acoustic instrument** that extracts statistical deviations from a speaker's learned baseline and presents them through an objective, non-intrusive UI interface. The user remains the sole expert interpreter.

---

## 2. Hardware Audio Capture & Pre-Processing

### 2.1 Audio Capture Parameters
*   **Sample Rate ($F_s$):** $44,100\text{ Hz}$ (PCM 16-bit Mono).
*   **Window Size:** $100\text{ ms}$ ($4,410\text{ samples}$).
*   **Overlap Step:** $50\text{ ms}$ ($2,205\text{ samples}$) $\rightarrow$ **20 Frames Per Second (FPS)**.

### 2.2 Hardware AGC Bypass
To prevent Android OS Automatic Gain Control (AGC) and hardware dynamic range compression from dampening vocal dynamics, `AndroidAudioRecorder` prioritizes audio sources in the following sequence:
1.  `MediaRecorder.AudioSource.VOICE_RECOGNITION`
2.  `MediaRecorder.AudioSource.UNPROCESSED` (Android 7.0+)
3.  `MediaRecorder.AudioSource.MIC` (Fallback)

---

## 3. Digital Signal Processing (DSP) & Acoustic Formulas

### 3.1 RMS Amplitude (Power)
Root Mean Square ($RMS$) measures vocal power and acoustic energy:

$$\text{RMS} = \frac{1}{32767} \sqrt{\frac{1}{N} \sum_{i=1}^{N} x_i^2}$$

*   Where $x_i$ is the 16-bit PCM short sample value (range $-32768 \dots 32767$).
*   If $\text{RMS} < 0.0015f$, the frame is classified as Silence / Non-Speech and bypassed.

### 3.2 High-Frequency Energy Ratio Noise Filter ($hfRatio$)
To filter out sub-bass mechanical noise (hand waving, phone handling < 30 Hz) while preserving 100% of human vocal fundamentals ($85\text{–}400\text{ Hz}$), the sample-to-sample derivative energy ratio is computed:

$$hfRatio = \frac{\sum_{i=1}^{N-1} (x_i - x_{i-1})^2}{\sum_{i=0}^{N-1} x_i^2}$$

*   **Hand Waving / Air Movement (< 30 Hz):** $hfRatio \approx 0.00001 \dots 0.00008$.
*   **Human Vocal Fundamental ($85\text{–}400\text{ Hz}$ at $44.1\text{ kHz}$):** $hfRatio \approx 0.00030 \dots 0.01000$.
*   **Gating Threshold:** If $hfRatio < 0.00012f$, pitch detection is aborted ($\text{Pitch} = 0\text{ Hz}$).

### 3.3 Pitch ($F_0$) Estimation & Parabolic Peak Interpolation
Fundamental frequency ($F_0$) is estimated using Normalized Autocorrelation ($r(k)$) over lag range $k \in [\text{minLag}, \text{maxLag}]$:

$$\text{minLag} = \left\lfloor \frac{F_s}{450} \right\rfloor = 98 \text{ samples}, \quad \text{maxLag} = \left\lfloor \frac{F_s}{85} \right\rfloor = 518 \text{ samples}$$

$$r(k) = \frac{\sum_{n=0}^{N-k-1} x[n] \cdot x[n+k]}{\sqrt{\sum_{n=0}^{N-k-1} x[n]^2 \cdot \sum_{n=0}^{N-k-1} x[n+k]^2}}$$

#### Parabolic Sub-sample Peak Interpolation (0.01 Hz Precision)
To eliminate integer lag quantization noise (which causes false Jitter artifacts), a parabola is fitted through the peak autocorrelation value $y_2 = r(k_{best})$ and its neighbors $y_1 = r(k_{best}-1)$, $y_3 = r(k_{best}+1)$:

$$\delta = \frac{y_1 - y_3}{2(2y_2 - y_1 - y_3)} \in [-0.5, 0.5]$$

$$k_{exact} = k_{best} + \delta$$

$$F_0 = \frac{F_s}{k_{exact}}$$

*   If $r(k_{best}) < 0.30f$, confidence is insufficient and $F_0 = 0\text{ Hz}$.

### 3.4 Vocal Cord Jitter (Micro-Tremors)
Jitter measures relative cycle-to-time perturbations of vocal cord vibrations:

$$\text{Jitter \%} = \frac{\frac{1}{M} \sum_{i=1}^{M} |F_0[i] - F_0[i-1]|}{\bar{F}_0} \times 100\%$$

*   Where $M$ is the count of valid pitch transitions with $\frac{|F_0[i] - F_0[i-1]|}{F_0[i-1]} < 0.25$.
*   Values $> 15\%$ are capped at $15\%$ (unvoiced noise threshold).

---

## 4. Calibration & Baselines

### 4.1 Short-Term Adaptive Baseline (`MovingBaseline`)
`MovingBaseline` tracks ambient noise floor and short-term vocal averages using Exponential Moving Averages (EMA):

$$\text{noiseFloor} \leftarrow \text{noiseFloor} \cdot (1 - \alpha_N) + \text{RMS} \cdot \alpha_N \quad (\alpha_N = 0.015)$$

$$\text{speechRMS} \leftarrow \text{speechRMS} \cdot (1 - \alpha_S) + \text{RMS} \cdot \alpha_S \quad (\alpha_S = 0.02)$$

$$\bar{F}_0 \leftarrow \bar{F}_0 \cdot (1 - \alpha_P) + F_0 \cdot \alpha_P \quad (\alpha_P = 0.02)$$

### 4.2 Global Session Profile (`GlobalProfile`)
`GlobalProfile` calculates long-term statistics against active speech frames ($F_0 \in [85, 450]\text{ Hz}$):
*   **RMS Mean ($\bar{RMS}_{global}$):** 50th percentile (Median) of speech RMS. Using the median prevents shouting outbursts from corrupting the baseline speech norm.
*   **Pitch Mean ($\bar{F}_{0, global}$):** 50th percentile (Median) of fundamental pitch.
*   **Jitter Mean ($\bar{Jitter}_{global}$):** 50th percentile (Median) of jitter percentage.
*   **Pitch Standard Deviation ($\sigma_{pitch}$):** Standard deviation in semitones:
    $$\Delta_{\text{semi}} = 12 \log_2 \left( \frac{F_0}{\bar{F}_{0, global}} \right)$$
*   **RMS Standard Deviation ($\sigma_{rms}$):** Standard deviation in decibels:
    $$\Delta_{\text{dB}} = 20 \log_{10} \left( \frac{\text{RMS}}{\bar{RMS}_{global}} \right)$$

---

## 5. Z-Score Stress Calculation & Look-Ahead Verification

### 5.1 Z-Score Normalization
Raw metrics are converted to standard deviations ($\sigma$) from global speech norms:

$$Z_{pitch} = \frac{\max\left(0, 12 \log_2 \left(\frac{F_0}{\bar{F}_{0, global}}\right)\right)}{\sigma_{pitch} \cdot D}$$

$$Z_{rms} = \frac{\max\left(0, 20 \log_{10} \left(\frac{\text{RMS}}{\bar{RMS}_{global}}\right)\right)}{\sigma_{rms}}$$

$$Z_{jitter} = \frac{\max\left(0, \text{Jitter} - \bar{\text{Jitter}}_{global}\right)}{\sigma_{jitter} \cdot D}$$

*   **Dynamic Headroom Damper ($D$):** During loud or aggressive speech, $D = \frac{\text{RMS}_{recent}}{\bar{RMS}_{global}} \cdot \frac{\sigma_{pitch, recent}}{\sigma_{pitch, global}}$ desensitizes pitch/jitter Z-scores to prevent emotional emphasis from triggering false stress anomalies.

### 5.2 Composite Stress Z-Score
$$Z_{total} = 0.35 \cdot Z_{jitter} + 0.30 \cdot Z_{pitch} + 0.35 \cdot Z_{rms}$$

### 5.3 Look-Ahead Buffer Verification (600ms)
A 600ms (12 atoms) look-ahead queue compares $Z_{total}$ to future frames. If the average future stress $Z_{future} < 0.70 \cdot Z_{total}$, the anomaly is identified as a transient acoustic glitch and dampened by $50\%$.

### 5.4 0..1 Score Scaling
$$Score = \min\left(1.0, \frac{Z}{5.0}\right)$$

---

## 6. Complete Prioritized State Matrix

The continuous headline text overlay displays statuses evaluated in strict priority order:

| Priority | Status Enum (`AnalysisStatus`) | Trigger Condition | Display Text | UI Color Token |
| :---: | :--- | :--- | :--- | :--- |
| **1** | `WARMUP` | `timestamp < 5000ms` | `Voice profiling in progress` | `TEXT_SECONDARY` (70% Alpha) |
| **2** | `CLIPPING` | $\text{RMS} > 0.85$ | `Microphone clipping` | `STATE_ERROR` |
| **2** | `LOW_SNR` | Confidence $< 0.20$ | `Low signal level` | `STATE_WARNING` |
| **3** | `DISORGANIZATION` | $JitterZ \ge 0.44 \land PitchZ \ge 0.44 \land RmsZ \ge 0.44$ | `Disorganized pitch state` | `TEXT_PRIMARY` |
| **3** | `PANIC` | $JitterZ \ge 0.44 \land PitchZ \ge 0.44$ | `Hidden panic reaction` | `TEXT_PRIMARY` |
| **3** | `AGGRESSION` | $JitterZ \ge 0.44 \land RmsZ \ge 0.44$ | `Aggressive speech tone` | `TEXT_PRIMARY` |
| **3** | `CONFRONTATION` | $PitchZ \ge 0.44 \land RmsZ \ge 0.44$ | `Open vocal confrontation` | `TEXT_PRIMARY` |
| **3** | `FEAR_SINGLE` | $JitterZ \ge 0.44$ | `Pronounced micro-tremor` | `TEXT_PRIMARY` |
| **3** | `STRESS_SINGLE` | $PitchZ \ge 0.44$ | `Cognitive stress spike` | `TEXT_PRIMARY` |
| **3** | `PRESSURE_SINGLE` | $RmsZ \ge 0.44$ | `Acoustic pressure spike` | `TEXT_PRIMARY` |
| **4** | `MILD_FLUCTUATION` | Any biomarker score $\ge 0.26$ | `Subtle emotional fluctuation` | `ACCENT_PRIMARY` |
| **5** | `CALM` | All biomarker scores $< 0.26$ | `Speech is steady` | `TEXT_SECONDARY` |

---

## 7. Data Flow & Execution Lifecycles

### 7.1 Live Recording Lifecycle
1.  **Audio Capture:** `AndroidAudioRecorder` emits 16-bit PCM short arrays to `rawAudioFlow`.
2.  **Atomization:** Audio is split into 100ms windows with 50ms overlap (20 FPS).
3.  **Real-time Processing:** `AudioAnalyzer.calculateHonestAnalysis` calculates Z-scores and returns clean 7-field `AudioFrame`.
4.  **Live Smoothing:** EMA smoothing ($\alpha = 0.18$) is applied to `stressScore`, `jitterScore`, `pitchScore`, `rmsScore`.
5.  **State Resolution:** `AnalysisStatusResolver.resolve` computes continuous `AnalysisStatus`.
6.  **Persistence:** Every 5 seconds (100 frames), `persistFrames` asynchronously writes `AudioFrame` batch to SQLite DB (`SessionFrame` table).
7.  **UI Rendering:** `AnalyzerSessionController` updates live gauges, appends 600ms clustered timeline markers, and emits `AnalyzerDisplaySnapshot`.

### 7.2 Save Session Lifecycle
1.  `stopAnalysis(save = true, anomalyCount)` is called.
2.  `deleteCalibrationData(sessionId)` deletes temporary `CalibrationFrame` DB rows.
3.  `SessionEntity` is updated with `duration`, `isCompleted = 1`, and `anomalyCount` ($O(1)$ instant save in $0.002\text{s}$).

### 7.3 Review Mode Lifecycle
1.  `repository.getFramesForSession(sessionId)` loads stored clean `AudioFrame`s from SQLite DB.
2.  Filters out initial 5s warmup phase (`timestamp >= 5000ms`).
3.  Loads frames directly into `AnalyzerSessionController`.
4.  **1:1 Fidelity:** Renders the exact same charts, gauges, and markers as Live Mode without any secondary re-analysis.

---

## 8. Clean `AudioFrame` Domain Model

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

## 9. UI & Rendering Engine

*   **Typewriter UI Engine:** Integrated `TypingText` composable types status phrases character-by-character (25ms char delay) without quotes or slashes, utilizing a ghost space-reservation layer (`Text(color = Transparent)`) to eliminate layout shifts.
*   **KeepScreenOn Multiplatform Lock:** `KeepScreenOn(keepOn = isAnalyzing && !isPaused)` prevents screen dimming/sleep during active recording via `LocalView.current.keepScreenOn` (Android) and `idleTimerDisabled` (iOS).
*   **Non-linear Intensity Curve ($x^{0.60}$):** `mapToUiIntensity` amplifies small physiological changes in the $0.05 \dots 0.30$ range for expressive visual feedback.
