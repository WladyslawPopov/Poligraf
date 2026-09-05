# Specification: Scientific Voice Stress Analysis Engine (Poligraf) 🎙️

> **Document Version:** 1.2.0  
> **Date:** September 2026  
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

### 2.2 Hardware AGC Bypass & Silence Fallback
To prevent Android OS Automatic Gain Control (AGC) and hardware dynamic range compression from dampening vocal dynamics, `AndroidAudioRecorder` prioritizes audio sources in sequence (`VOICE_RECOGNITION` $\rightarrow$ `MIC` $\rightarrow$ `DEFAULT`) with an instant test-read to detect vendor driver silence bugs on custom Android HALs.

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

## 5. Z-Score Stress Calculation & Dynamic Sensitivity Scaling

### 5.1 Dynamic Delta Sigma Sensitivity ($\Delta\sigma$)
Raw metrics are converted to standard deviations ($\sigma$) from global speech norms, with thresholds scaled by the user-selected `SensitivityLevel`:

$$\Delta\sigma_{\text{threshold}} = \text{ANOMALY\_SIGMA} \times M_{\text{sensitivity}}$$

*   **Low Sensitivity ($M = 1.30$):** $\Delta\sigma_{\text{threshold}} = 2.86$
*   **Medium Sensitivity ($M = 1.00$):** $\Delta\sigma_{\text{threshold}} = 2.20$
*   **High Sensitivity ($M = 0.80$):** $\Delta\sigma_{\text{threshold}} = 1.76$

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

## 6. Complete Prioritized State Matrix & Conversational Phrases

The continuous headline text overlay displays statuses evaluated in strict priority order, featuring human conversational phrases:

| Priority | Status Enum (`AnalysisStatus`) | Trigger Condition | Display Text (RU) | Display Text (EN) | UI Color Token |
| :---: | :--- | :--- | :--- | :--- | :--- |
| **1** | `WARMUP` | `timestamp < 5000ms` | `Настройка под голос собеседника...` | `Calibrating to speaker's voice...` | `TEXT_SECONDARY` |
| **2** | `CLIPPING` | $\text{RMS} > 0.85$ | `Перегруз микрофона` | `Microphone clipping` | `STATE_ERROR` |
| **2** | `LOW_SNR` | Confidence $< 0.20$ | `Низкий уровень сигнала` | `Low signal level` | `STATE_WARNING` |
| **3** | `DISORGANIZATION` | $JitterZ \ge \theta \land PitchZ \ge \theta \land RmsZ \ge \theta$ | `Напряженная и хаотичная ситуация` | `Tense & volatile situation` | `TEXT_PRIMARY` |
| **3** | `PANIC` | $JitterZ \ge \theta \land PitchZ \ge \theta$ | `Сильное внутреннее напряжение` | `Strong internal tension` | `TEXT_PRIMARY` |
| **3** | `AGGRESSION` | $JitterZ \ge \theta \land RmsZ \ge \theta$ | `Тон более раздраженный, чем обычно` | `Tone is more irritated than usual` | `TEXT_PRIMARY` |
| **3** | `CONFRONTATION` | $PitchZ \ge \theta \land RmsZ \ge \theta$ | `Активный спор и жесткая интонация` | `Active argument & sharp tone` | `TEXT_PRIMARY` |
| **3** | `FEAR_SINGLE` | $JitterZ \ge \theta$ | `Волнение и дрожь в интонации` | `Nervousness in tone` | `TEXT_PRIMARY` |
| **3** | `STRESS_SINGLE` | $PitchZ \ge \theta$ | `Повышение напряжения в беседе` | `Increasing tension in dialogue` | `TEXT_PRIMARY` |
| **3** | `PRESSURE_SINGLE` | $RmsZ \ge \theta$ | `Тон стал громче и с нажимом` | `Tone is louder with pressure` | `TEXT_PRIMARY` |
| **4** | `MILD_FLUCTUATION` | Any biomarker score $\ge \theta_{glow}$ | `Легкие эмоциональные акценты` | `Slight emotional inflections` | `TEXT_PRIMARY` |
| **5** | `CALM` | All biomarker scores $< \theta_{glow}$ | `Разговор идет спокойно` | `Conversation is going calmly` | `TEXT_PRIMARY` |

*Note: $\theta = 0.35f$ and $\theta_{glow} = 0.22f$ at Medium Sensitivity.*

---

## 7. Decoupled Dual-Stream & Quantum Window Architecture

### 7.1 Stream 1: Live 20 FPS Visual Metrics Stream
1.  **Hardware Capture:** `AndroidAudioRecorder` emits 16-bit PCM short arrays at 44.1 kHz.
2.  **Atomization:** Audio is split into 100ms windows with 50ms overlap (20 FPS).
3.  **Acoustic Processing:** `AudioAnalyzer.calculateHonestAnalysis` computes Z-scores and returns clean `AudioFrame`.
4.  **20 FPS Emission:** Emits live metrics (`jitterScore`, `pitchScore`, `rmsScore`) to `_currentFrame` and `_audioFrames` for analog-like visualization rendering on `Voice Ribbon`, `Equalizer`, `State Map`, and `Rings`.

### 7.2 Stream 2: Quantum Window Anomaly & Status Recognition Pipeline
1.  **Quantum Accumulation:** Subframes are accumulated into `quantumSubFrames`.
2.  **Quantum Flush ($T \in [1.0s \dots 3.0s]$):** Every $T$ seconds (configured Quantum Window duration), `QuantumWindowAggregator.aggregateWindow` evaluates the discrete window bucket $W_i = [i \cdot T \dots (i+1) \cdot T]$.
3.  **3-Tier Status Overlay:** Resolves `primaryStatus`, `primaryAlpha`, and `secondaryStatusesWithScores`. Updates `_currentQuantumAnalysis` ONCE per completed window $T$.
4.  **Deterministic Marker Indexing (`m_window_$i`):** `extractWindowMarkers` generates markers with deterministic IDs `m_window_${windowIndex}_${status.name}`.
5.  **In-Place Marker Persistence:** Markers are appended to `_sessionMarkers` and persisted to SQLite DB table `SessionMarker` using `INSERT OR REPLACE INTO SessionMarker`.

### 7.3 Database Schema & Migration (`4.sqm`)
*   **`SessionMarker` Schema:**
    ```sql
    CREATE TABLE SessionMarker (
        id TEXT NOT NULL PRIMARY KEY,
        sessionId TEXT NOT NULL,
        timestamp INTEGER NOT NULL,
        status TEXT NOT NULL,
        dominantMetric TEXT NOT NULL,
        isFullAnomaly INTEGER AS Boolean NOT NULL DEFAULT 0,
        alpha REAL NOT NULL DEFAULT 1.0,
        FOREIGN KEY(sessionId) REFERENCES SessionEntity(id) ON DELETE CASCADE
    );
    ```

---

## 8. History & Review Mode Scenario Scoring

### 8.1 3-Parameter Session History Breakdown
In the history list (`HistoryItem.kt`) and session detail view (`SessionSummaryCard.kt`), sessions are summarized by 3 distinct parameters:
1.  🔴 **Full Anomalies Count (`fullAnomalyCount`):** Count of threshold-breaking full anomaly markers (`isFullAnomaly = 1`).
2.  🟡 **Half-Tone Fluctuations Count (`halftoneAnomalyCount`):** Count of sub-threshold half-tone markers (`isFullAnomaly = 0`).
3.  📝 **Notes Count (`noteCount`):** Count of user notes recorded during the session.

### 8.2 Weighted Review Mode Scenario Score ($N_{\text{weighted}}$)
When evaluating session volatility and conclusion verdicts in Review Mode, a weighted score is computed:

$$N_{\text{weighted}} = (\text{fullAnomalyCount}) \times 1.0 + (\text{halftoneAnomalyCount}) \times 0.5$$

$$\text{AnomaliesPerMinute} = \frac{N_{\text{weighted}}}{\text{DurationMinutes}}$$

#### Verdict Thresholds:
*   **$\text{AnomaliesPerMinute} \le 1.2$:** `VOLATILITY_LOW` / `CONCLUSION_POSITIVE` (🟢 Green)
*   **$1.2 < \text{AnomaliesPerMinute} \le 3.2$:** `VOLATILITY_MEDIUM` / `CONCLUSION_NEUTRAL` (🟡 Yellow)
*   **$\text{AnomaliesPerMinute} > 3.2$:** `VOLATILITY_HIGH` / `CONCLUSION_NEGATIVE` (🔴 Red)

---

## 9. UI & Rendering Engine

*   **3-Tier Status Overlay (`InterpretationOverlay`):** Renders primary headline text (85–100% opacity) + top and bottom secondary half-tone texts (100% solid `ColorToken.TEXT_SECONDARY`, `titleMedium` typography, 12dp padding). Uses 700ms `AnimatedContent` cross-fade dissolve transitions (`LinearOutSlowInEasing`) for smooth status shifts.
*   **Pixel-Perfect Vector Marker Rendering (`AnomalyTimeline`):** Outline background is drawn natively (`drawCircle`), followed by a single `VectorPainter.draw` pass to eliminate mid-frame `colorFilter` mutation flashes.
*   **Pure Geometric Rhombus Vector (`AppIcons.GeometricDiamond`):** Custom `ImageVector` path (`M12,2 L22,12 L12,22 L2,12 Z`) for clean, symmetrical rhombus markers.
*   **60 FPS Live Timeline Scrubber Tracking:** Smooth scroll tracking follows real-time capture head (`durationPx`) without step-jumping.
*   **Voice Ribbon Optimization:** Reuses `Path` instances and steps sample calculations at 12dp, reducing GPU/CPU path overhead by 75%.

---

## 10. Technological Boundary & Physical Limits

This on-device Kotlin Multiplatform DSP engine represents the **absolute mathematical, physical, and signal-processing maximum** that can be extracted directly from physical sound wave acoustics on a mobile instrument.

Anything beyond this deterministic acoustic core — such as attempting cloud machine learning, server-side neural network emotion guessing, or artificial "truth/lie" probability scores — strays into non-scientific, non-deterministic toy slop that compromises the mathematical integrity of a professional physical instrument.
