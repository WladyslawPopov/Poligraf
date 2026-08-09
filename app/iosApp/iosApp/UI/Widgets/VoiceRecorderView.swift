import SwiftUI
import SharedLogic

private let appleRed = Color(red: 255/255, green: 59/255, blue: 48/255)
private let appleBlue = Color(red: 0/255, green: 122/255, blue: 255/255)
private let darkGrayBg = Color(red: 28/255, green: 28/255, blue: 30/255)

struct VoiceRecorderView: View {
    let widget: UiWidget.VoiceRecorder
    let designSystem: DesignSystem
    
    // Callbacks
    let onToggle: () -> Void
    let onStop: () -> Void
    let onPlay: () -> Void
    let onPause: () -> Void
    let onSeek: (Int64) -> Void
    let onTrimUpdate: (Int64, Int64) -> Void
    let onTrimCancel: () -> Void
    let onTrimApply: (Int64, Int64) -> Void
    let onReplace: () -> Void
    let onSave: () -> Void
    let onResume: () -> Void
    let onToggleTrim: () -> Void
    let onSkip: (Int64) -> Void
    
    var body: some View {
        VStack(spacing: 0) {
            if widget.isExpanded {
                expandedContent
            } else {
                collapsedContent
            }
        }
        .background(widget.isExpanded ? Color.black : darkGrayBg)
        .cornerRadius(32, corners: [.topLeft, .topRight])
        .shadow(color: Color.black.opacity(0.3), radius: 20, x: 0, y: -5)
        .animation(.spring(), value: widget.isExpanded)
    }
    
    private var collapsedContent: some View {
        VStack(spacing: 12) {
            // Drag handle
            Capsule()
                .fill(Color.white.opacity(0.3))
                .frame(width: 36, height: 5)
                .padding(.top, 12)

            Text(widget.title)
                .font(.system(size: 17, weight: .bold))
                .foregroundColor(.white)
            
            Text(formatDurationSimple(millis: widget.durationMillis))
                .font(.system(size: 13))
                .foregroundColor(.white.opacity(0.7))
            
            // Mini Waveform
            MiniWaveformView(widget: widget)
                .frame(height: 32)
                .padding(.horizontal, 40)
            
            // Stop Button (Red Circle with White Square)
            Button(action: onStop) {
                ZStack {
                    Circle()
                        .fill(appleRed)
                        .frame(width: 48, height: 48)
                    RoundedRectangle(cornerRadius: 4)
                        .fill(.white)
                        .frame(width: 18, height: 18)
                }
            }
            .padding(.bottom, 24)
        }
        .frame(maxWidth: .infinity)
    }
    
    private var expandedContent: some View {
        VStack(spacing: 0) {
            // Header / Toolbar
            HStack {
                if widget.isTrimming {
                    Button("Cancel") {
                        onTrimCancel()
                    }
                    .font(.system(size: 17))
                    .foregroundColor(appleBlue)
                    
                    Spacer()
                    
                    Text("Trim")
                        .font(.system(size: 17, weight: .bold))
                        .foregroundColor(.white)
                    
                    Spacer()
                    
                    Button("Apply") {
                        onTrimApply(widget.trimStartMillis, widget.trimEndMillis)
                    }
                    .font(.system(size: 17, weight: .bold))
                    .foregroundColor(appleBlue)
                } else {
                    Button(action: {}) {
                        Image(systemName: "ellipsis.circle")
                            .font(.system(size: 22))
                            .foregroundColor(appleBlue)
                    }
                    
                    Spacer()
                    
                    VStack(spacing: 2) {
                        Text(widget.title)
                            .font(.system(size: 17, weight: .bold))
                            .foregroundColor(.white)
                        Text("Today at \(formatTimeShort()) \(formatDurationSimple(millis: widget.durationMillis))")
                            .font(.system(size: 13))
                            .foregroundColor(.white.opacity(0.6))
                    }
                    
                    Spacer()
                    
                    Button(action: onSave) {
                        Image(systemName: "checkmark.circle.fill")
                            .font(.system(size: 28))
                            .foregroundColor(appleBlue)
                    }
                }
            }
            .padding(.horizontal, 20)
            .frame(height: 60)
            .padding(.top, 8)
            
            Spacer().frame(height: 24)
            
            // Professional Waveform
            if !widget.isTrimming {
                ProfessionalWaveformView(
                    widget: widget,
                    designSystem: designSystem,
                    onSeek: onSeek,
                    onTrimUpdate: onTrimUpdate
                )
                .frame(height: 220)
                .padding(.horizontal, 16)
                .cornerRadius(24)
            } else {
                Spacer().frame(height: 220)
            }
            
            Spacer().frame(height: 32)
            
            // Time Display
            let timerValue = (widget.status == .review || widget.isTrimming) ? widget.playbackPositionMillis : widget.durationMillis
            Text(formatDurationPrecise(millis: timerValue))
                .font(.system(size: 72, weight: .medium, design: .monospaced))
                .foregroundColor(.white)
            
            if widget.isTrimming {
                Spacer().frame(height: 48)
                // We could add MiniTrimOverview here if needed, but keeping it simple for now
                Spacer().frame(height: 48)
            } else if widget.status == .review || widget.status == .paused {
                Spacer().frame(height: 32)
                reviewControls
            }
            
            Spacer()
            
            // Interaction Area - Bottom Action Bar
            HStack(spacing: 0) {
                if widget.isTrimming {
                    Button(action: {}) {
                        Text("Trim")
                            .font(.system(size: 17, weight: .semibold))
                            .foregroundColor(.white)
                            .padding(.horizontal, 32)
                            .padding(.vertical, 12)
                            .background(Color.white.opacity(0.1))
                            .clipShape(Capsule())
                    }
                    
                    Spacer()
                    
                    Button(action: {}) {
                        Text("Delete")
                            .font(.system(size: 17, weight: .semibold))
                            .foregroundColor(appleRed)
                    }
                } else {
                    Button(action: {}) {
                        Image(systemName: "note.text")
                            .font(.system(size: 24))
                            .foregroundColor(appleBlue)
                    }
                    
                    Spacer()
                    
                    // Central Button (Red Oval)
                    Button(action: {
                        if widget.status == .review || widget.status == .paused {
                            onReplace()
                        } else {
                            onStop()
                        }
                    }) {
                        ZStack {
                            Capsule()
                                .fill(appleRed)
                                .frame(width: 140, height: 64)
                            
                            if widget.status == .review || widget.status == .paused {
                                Text("REPLACE")
                                    .font(.system(size: 16, weight: .bold))
                                    .foregroundColor(.white)
                                    .kerning(1)
                            } else if widget.status == .recording {
                                // White Pause bars
                                HStack(spacing: 6) {
                                    RoundedRectangle(cornerRadius: 2)
                                        .fill(.white)
                                        .frame(width: 6, height: 24)
                                    RoundedRectangle(cornerRadius: 2)
                                        .fill(.white)
                                        .frame(width: 6, height: 24)
                                }
                            } else {
                                // Stop Square
                                RoundedRectangle(cornerRadius: 4)
                                    .fill(.white)
                                    .frame(width: 24, height: 24)
                            }
                        }
                    }
                    
                    Spacer()
                    
                    Button(action: onToggleTrim) {
                        Image(systemName: "scissors")
                            .font(.system(size: 24))
                            .foregroundColor(appleBlue)
                    }
                }
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 32)
        }
    }
    
    private var reviewControls: some View {
        HStack(spacing: 48) {
            Button(action: { onSkip(-15000) }) {
                Image(systemName: "gobackward.15")
                    .font(.system(size: 36))
                    .foregroundColor(.white)
            }
            
            Button(action: widget.isPlaying ? onPause : onPlay) {
                Image(systemName: widget.isPlaying ? "pause.fill" : "play.fill")
                    .font(.system(size: 56))
                    .foregroundColor(.white)
            }
            
            Button(action: { onSkip(15000) }) {
                Image(systemName: "goforward.15")
                    .font(.system(size: 36))
                    .foregroundColor(.white)
            }
        }
    }
    
    private func formatDurationPrecise(millis: Int64) -> String {
        let ms = (millis % 1000) / 10
        let seconds = (millis / 1000) % 60
        let minutes = (millis / (1000 * 60)) % 60
        return String(format: "%02d:%02d,%02d", minutes, seconds, ms)
    }
    
    private func formatDurationSimple(millis: Int64) -> String {
        let seconds = (millis / 1000) % 60
        let minutes = (millis / (1000 * 60))
        return String(format: "%d:%02d", minutes, seconds)
    }
    
    private func formatTimeShort() -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm"
        return formatter.string(from: Date())
    }
}

struct MiniWaveformView: View {
    let widget: UiWidget.VoiceRecorder
    
    var body: some View {
        GeometryReader { geometry in
            let width = geometry.size.width
            let height = geometry.size.height
            let centerY = height / 2
            let step: CGFloat = 4
            let count = Int(width / step)
            
            let amplitudes = widget.amplitudes.map { $0 as? Float ?? 0.0 }
            
            ZStack {
                // Red baseline
                Path { path in
                    path.move(to: CGPoint(x: 0, y: centerY))
                    path.addLine(to: CGPoint(x: width, y: centerY))
                }
                .stroke(appleRed.opacity(0.3), lineWidth: 1)
                
                // Bars
                Path { path in
                    for i in 0..<count {
                        let x = CGFloat(i) * step
                        let amp = (widget.status == .recording) 
                            ? (amplitudes.indices.contains(amplitudes.count - count + i) ? amplitudes[amplitudes.count - count + i] : 0.05)
                            : 0.05
                        
                        let h = max(2, CGFloat(amp) * height)
                        let rect = CGRect(x: x, y: centerY - h / 2, width: 2, height: h)
                        path.addRoundedRect(in: rect, cornerSize: CGSize(width: 1, height: 1))
                    }
                }
                .fill(appleRed)
            }
        }
    }
}

struct ProfessionalWaveformView: View {
    let widget: UiWidget.VoiceRecorder
    let designSystem: DesignSystem
    let onSeek: (Int64) -> Void
    let onTrimUpdate: (Int64, Int64) -> Void
    
    private let step: CGFloat = 6
    @State private var dragBaseMillis: Int64? = nil
    
    var body: some View {
        GeometryReader { geometry in
            let width = geometry.size.width
            let height = geometry.size.height
            let midX = width / 2
            
            let amplitudes = widget.amplitudes.map { $0 as? Float ?? 0.0 }
            let currentIdx = Int(widget.playbackPositionMillis / 33)
            
            ZStack {
                Color(red: 28/255, green: 28/255, blue: 30/255) // Dark Gray Background for waveform area
                
                // Waveform bars
                Path { path in
                    for (index, amp) in amplitudes.enumerated() {
                        let x = midX + CGFloat(index - currentIdx) * step
                        if x > -step && x < width + step {
                            let barHeight = max(4, CGFloat(amp) * height * 0.7)
                            let rect = CGRect(x: x, y: (height - barHeight) / 2, width: 3, height: barHeight)
                            path.addRoundedRect(in: rect, cornerSize: CGSize(width: 1.5, height: 1.5))
                        }
                    }
                }
                .fill(Color.white.opacity(0.3))
                
                // Playhead
                Rectangle()
                    .fill(appleRed)
                    .frame(width: 2)
                    .overlay(
                        VStack {
                            Circle().fill(appleRed).frame(width: 8, height: 8)
                            Spacer()
                            Circle().fill(appleRed).frame(width: 8, height: 8)
                        }
                        .offset(y: 0)
                        .frame(height: height)
                    )
                    .position(x: midX, y: height / 2)
            }
            .contentShape(Rectangle())
            .gesture(
                DragGesture()
                    .onChanged { value in
                        if (widget.status == .review || widget.status == .paused) && !widget.isTrimming {
                            if dragBaseMillis == nil {
                                dragBaseMillis = widget.playbackPositionMillis
                            }
                            let diffMillis = Int64((-value.translation.width / step) * 33)
                            let newPos = max(0, min(widget.durationMillis, (dragBaseMillis ?? 0) + diffMillis))
                            onSeek(newPos)
                        }
                    }
                    .onEnded { _ in
                        dragBaseMillis = nil
                    }
            )
        }
    }
}

// Extensions for styling
extension View {
    func cornerRadius(_ radius: CGFloat, corners: UIRectCorner) -> some View {
        clipShape(RoundedCorner(radius: radius, corners: corners))
    }
}

struct RoundedCorner: Shape {
    var radius: CGFloat = .infinity
    var corners: UIRectCorner = .allCorners

    func path(in rect: CGRect) -> Path {
        let path = UIBezierPath(roundedRect: rect, byRoundingCorners: corners, cornerRadii: CGSize(width: radius, height: radius))
        return Path(path.cgPath)
    }
}
