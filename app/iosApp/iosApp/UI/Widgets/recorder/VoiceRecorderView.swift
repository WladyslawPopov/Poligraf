import SwiftUI
import SharedLogic

struct VoiceRecorderView: View {
    @ObservedObject private var engine: NativeVoiceRecorderEngine
    @State private var isTrimMode = false
    @State private var trimStart: TimeInterval = 0
    @State private var trimEnd: TimeInterval = 10 
    @State private var isDraggingMiniTrim = false
    
    let state: VoiceRecorderUiState
    let designSystem: DesignSystem
    let onAction: (VoiceRecorderAction) -> Void
    let onSave: (URL, Int64, [Float]) -> Void

    init(
        state: VoiceRecorderUiState,
        designSystem: DesignSystem,
        onAction: @escaping (VoiceRecorderAction) -> Void,
        onSave: @escaping (URL, Int64, [Float]) -> Void
    ) {
        self.state = state
        self.designSystem = designSystem
        self.onAction = onAction
        self.onSave = onSave
        // Use the shared engine that is hooked to the KMP bridge
        self._engine = ObservedObject(wrappedValue: AppCoordinator.shared.voiceRecorderEngine!)
    }

    var body: some View {
        AppSheetContainer(
            designSystem: designSystem,
            titleToken: nil,
            onUserClose: {
                if !engine.isRecording { onAction(VoiceRecorderAction.DiscardActive()) }
            }
        ) {
            VStack(spacing: 0) {
                // Header (Match Android: Close & Menu on Left, Title Center, Save on Right)
                headerSection
                    .padding(.horizontal, 20)
                    .padding(.top, 12)

                Spacer().frame(height: 32)
                
                // Professional Waveform (60fps Canvas)
                VoiceRecorderProfessionalWaveformView(
                    engine: engine,
                    isTrimMode: isTrimMode,
                    trimStart: $trimStart,
                    trimEnd: $trimEnd,
                    designSystem: designSystem,
                    onSeek: { time in engine.seek(to: time) }
                )
                .frame(height: 240)
                .background(designSystem.color(.surface).opacity(0.3))
                
                if isTrimMode {
                    VoiceRecorderMiniTrimOverview(
                        state: state,
                        designSystem: designSystem,
                        trimStart: $trimStart,
                        trimEnd: $trimEnd,
                        isDragging: $isDraggingMiniTrim,
                        onAction: onAction
                    )
                    .padding(.horizontal, 20)
                    .padding(.top, 24)
                }

                Spacer()
                
                // Timer Section
                VStack(spacing: 4) {
                    Text(state.header.timerLabel)
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(designSystem.color(.textPrimary).opacity(0.4))
                    
                    Text(formatPreciseTime(engine.currentTime))
                        .font(.system(size: 56, weight: .medium, design: .monospaced))
                        .foregroundColor(designSystem.color(.textPrimary))
                }
                .padding(.bottom, 32)

                // Controls Row 1 (Playback)
                HStack(spacing: 48) {
                    Button(action: { onAction(VoiceRecorderAction.Skip(millis: -15000)) }) {
                        Image(systemName: "gobackward.15").font(.system(size: 28, weight: .light))
                    }
                    
                    Button(action: { onAction(VoiceRecorderAction.TogglePlay()) }) {
                        Image(systemName: state.controls.isPlaying ? "pause.fill" : "play.fill")
                            .font(.system(size: 44, weight: .light))
                            .frame(width: 44)
                    }

                    Button(action: { onAction(VoiceRecorderAction.Skip(millis: 15000)) }) {
                        Image(systemName: "goforward.15").font(.system(size: 28, weight: .light))
                    }
                }
                .foregroundColor(designSystem.color(.textPrimary))
                
                Spacer().frame(height: 48)
                
                // Controls Row 2 (Big Red Button or Trim Apply)
                if !isTrimMode {
                    recordButton
                } else {
                    trimActionButton
                }
                
                Spacer().frame(height: 32)
            }
            .animation(.easeInOut, value: engine.isRecording)
            .onAppear { initializeEngine() }
            .onChange(of: engine.duration) {
                if !isTrimMode { trimEnd = engine.duration }
            }
            .onChange(of: state.trim.startMillis) {
                if isDraggingMiniTrim { return }
                let time = Double(state.trim.startMillis) / 1000.0
                if abs(trimStart - time) > 0.01 { trimStart = time }
            }
            .onChange(of: state.trim.endMillis) {
                if isDraggingMiniTrim { return }
                let time = Double(state.trim.endMillis) / 1000.0
                if abs(trimEnd - time) > 0.01 { trimEnd = time }
            }
        }
    }
    
    private var headerSection: some View {
        HStack(spacing: 12) {
            if isTrimMode {
                Button(designSystem.string(token: .recorderCancel)) { 
                    withAnimation { isTrimMode = false }
                }
                .foregroundColor(designSystem.color(.textPrimary))
                
                Spacer()
                
                Text(state.header.title)
                    .font(.system(size: 17, weight: .bold))
                
                Spacer()
                
                // Placeholder for balance
                Color.clear.frame(width: 60, height: 1)
            } else {
                // Left Side: Close and Menu
                HStack(spacing: 10) {
                    Button(action: { onAction(VoiceRecorderAction.DiscardActive()) }) {
                        Image(systemName: "xmark").font(.system(size: 14, weight: .bold))
                            .foregroundColor(designSystem.color(token: .textPrimary).opacity(0.6))
                            .padding(8).background(designSystem.color(token: .textPrimary).opacity(0.1)).clipShape(Circle())
                    }
                    
                    Menu {
                        Button(designSystem.string(token: .recorderTrimMode)) {
                            withAnimation {
                                isTrimMode = true
                                trimStart = 0
                                trimEnd = engine.duration
                            }
                        }
                    } label: {
                        Image(systemName: "ellipsis.circle").font(.system(size: 22))
                            .foregroundColor(designSystem.color(token: .recorderSecondary))
                            .padding(6)
                            .background(designSystem.color(token: .recorderSecondary).opacity(0.15))
                            .clipShape(Circle())
                    }
                }
                
                Spacer()
                
                // Center: Title
                VStack(spacing: 2) {
                    Text(state.header.title).font(.system(size: 17, weight: .black))
                    Text(state.header.subtitle).font(.system(size: 13, weight: .medium))
                        .foregroundColor(designSystem.color(.textPrimary).opacity(0.5))
                }
                
                Spacer()
                
                // Right Side: Save
                Button(action: { 
                    engine.stopRecording { url, duration, amplitudes in
                        onSave(url, duration, amplitudes)
                    }
                }) {
                    Image(systemName: "checkmark").font(.system(size: 18, weight: .bold))
                        .foregroundColor(designSystem.color(token: .recorderSecondary))
                        .frame(width: 36, height: 36).background(designSystem.color(token: .recorderSecondary).opacity(0.15)).clipShape(Circle())
                }
            }
        }
    }
    
    private var recordButton: some View {
        Button(action: {
            onAction(VoiceRecorderAction.ToggleRecord())
        }) {
            ZStack {
                Circle().stroke(designSystem.color(.textPrimary).opacity(0.15), lineWidth: 4).frame(width: 82, height: 82)
                if state.controls.isRecording {
                    RoundedRectangle(cornerRadius: 8).fill(designSystem.color(.recorderPrimary)).frame(width: 32, height: 32)
                } else {
                    Circle().fill(designSystem.color(.recorderPrimary)).frame(width: 68, height: 68)
                }
            }
        }
    }
    
    private var trimActionButton: some View {
        Button(action: {
            onAction(VoiceRecorderAction.ApplyTrim(start: Int64(trimStart * 1000), end: Int64(trimEnd * 1000)))
            withAnimation { isTrimMode = false }
        }) {
            Text(designSystem.string(token: .recorderTrim))
                .font(.system(size: 17, weight: .bold)).foregroundColor(designSystem.color(.textPrimary))
                .padding(.horizontal, 60).padding(.vertical, 14)
                .background(designSystem.color(.textPrimary).opacity(0.15)).clipShape(Capsule())
        }
    }

    private func initializeEngine() {
        if let path = state.filePath {
            engine.loadAudio(path: path, amplitudes: state.waveform.amplitudes)
            trimEnd = engine.duration
        }
    }

    private func formatPreciseTime(_ time: TimeInterval) -> String {
        let minutes = Int(time) / 60
        let seconds = Int(time) % 60
        let centiseconds = Int((time.truncatingRemainder(dividingBy: 1)) * 100)
        return String(format: "%02d:%02d.%02d", minutes, seconds, centiseconds)
    }
}
