import SwiftUI
import SharedLogic

struct VoiceRecorderView: View {
    let widget: UiWidget.VoiceRecorder
    let designSystem: DesignSystem
    let onToggle: () -> Void
    let onStop: () -> Void
    
    var body: some View {
        VStack(spacing: 16) {
            // 1. Status and Timer
            HStack {
                StatusBadge(status: widget.status, designSystem: designSystem)
                Spacer()
                Text(formatDuration(millis: widget.durationMillis))
                    .font(.title2)
                    .fontWeight(.bold)
                    .foregroundColor(designSystem.color(.textPrimary))
            }
            
            // 2. Waveform Visualization
            WaveformView(
                amplitudes: widget.amplitudes.map { $0 as? Float ?? 0.0 },
                color: designSystem.color(.accentPrimary)
            )
            .frame(height: 60)
            
            // 3. Controls
            if widget.status != .finished {
                HStack(spacing: 20) {
                    Button(action: onToggle) {
                        Image(systemName: widget.status == .paused ? designSystem.icon(.play) : designSystem.icon(.pause))
                            .font(.title3)
                            .foregroundColor(designSystem.color(.textPrimary))
                            .frame(width: 48, height: 48)
                            .background(designSystem.color(.surfaceVariant))
                            .clipShape(Circle())
                    }
                    
                    Button(action: onStop) {
                        HStack {
                            Image(systemName: designSystem.icon(.check))
                            Text("Finish")
                                .fontWeight(.semibold)
                        }
                        .foregroundColor(designSystem.color(.textInverted))
                        .padding(.horizontal, 20)
                        .frame(height: 48)
                        .background(designSystem.color(.accentPrimary))
                        .clipShape(Capsule())
                    }
                }
            }
        }
        .padding(20)
    }
    
    private func formatDuration(millis: Int64) -> String {
        let seconds = (millis / 1000) % 60
        let minutes = (millis / (1000 * 60)) % 60
        return String(format: "%02d:%02d", minutes, seconds)
    }
}

struct StatusBadge: View {
    let status: UiWidget.VoiceRecorderStatus
    let designSystem: DesignSystem
    
    var body: some View {
        HStack(spacing: 6) {
            if status == .recording {
                Circle()
                    .fill(designSystem.color(.error))
                    .frame(width: 8, height: 8)
                    .opacity(1.0) // Animation could be added here
            }
            Text(statusText)
                .font(.caption)
                .fontWeight(.medium)
                .foregroundColor(statusColor)
        }
        .padding(.horizontal, 10)
        .padding(.vertical, 6)
        .background(statusColor.opacity(0.1))
        .clipShape(Capsule())
    }
    
    private var statusText: String {
        switch status {
        case .recording: return "Recording"
        case .paused: return "Paused"
        case .finished: return "Finished"
        default: return "Idle"
        }
    }
    
    private var statusColor: Color {
        switch status {
        case .recording: return designSystem.color(.error)
        case .paused: return designSystem.color(.warning)
        default: return designSystem.color(.textPrimary).opacity(0.5)
        }
    }
}

struct WaveformView: View {
    let amplitudes: [Float]
    let color: Color
    
    var body: some View {
        GeometryReader { geometry in
            HStack(alignment: .center, spacing: 2) {
                ForEach(0..<amplitudes.count, id: \.self) { index in
                    RoundedRectangle(cornerRadius: 2)
                        .fill(color)
                        .frame(width: 4, height: max(4, CGFloat(amplitudes[index]) * geometry.size.height))
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .trailing)
        }
    }
}
