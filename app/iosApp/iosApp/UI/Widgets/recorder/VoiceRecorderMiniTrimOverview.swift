import SwiftUI
import SharedLogic

struct VoiceRecorderMiniTrimOverview: View {
    let state: VoiceRecorderUiState
    let designSystem: DesignSystem
    @Binding var trimStart: TimeInterval
    @Binding var trimEnd: TimeInterval
    @Binding var isDragging: Bool
    let onAction: (VoiceRecorderAction) -> Void
    
    var body: some View {
        GeometryReader { geometry in
            let handleWidth: CGFloat = 24
            let width = geometry.size.width
            let sidePadding: CGFloat = handleWidth / 2 
            let trackWidth = width - handleWidth
            let duration = max(0.1, Double(state.waveform.durationMillis) / 1000.0)
            
            let startRatio = trimStart / duration
            let endRatio = trimEnd / duration
            let leftX = sidePadding + trackWidth * startRatio
            let rightX = sidePadding + trackWidth * endRatio
            
            ZStack(alignment: .leading) {
                // 1. Background Track (Static)
                ZStack {
                    RoundedRectangle(cornerRadius: 12)
                        .fill(designSystem.color(.background).opacity(0.5))
                    
                    miniWaveformCanvas(size: CGSize(width: trackWidth, height: geometry.size.height))
                        .opacity(0.3)
                }
                .frame(width: trackWidth)
                .offset(x: sidePadding)
                
                // 2. Dimmed Inactive Areas (The "Cut" parts)
                Group {
                    // Left Dim
                    Rectangle()
                        .fill(Color.black.opacity(0.5))
                        .frame(width: max(0, leftX - sidePadding))
                        .offset(x: sidePadding)
                    
                    // Right Dim
                    Rectangle()
                        .fill(Color.black.opacity(0.5))
                        .frame(width: max(0, (sidePadding + trackWidth) - rightX))
                        .offset(x: rightX)
                }
                .allowsHitTesting(false)
                
                // 3. Selection Box (The "Keep" part)
                ZStack {
                    // Yellow Highlight
                    Rectangle()
                        .fill(designSystem.color(.recorderAccent).opacity(0.15))
                    
                    // ONLY Top and Bottom borders (Sides are covered by handles)
                    VStack {
                        Rectangle()
                            .fill(designSystem.color(.recorderAccent))
                            .frame(height: 3) // Thicker line for better visibility
                        Spacer()
                        Rectangle()
                            .fill(designSystem.color(.recorderAccent))
                            .frame(height: 3)
                    }
                }
                .frame(width: max(0, rightX - leftX))
                .offset(x: leftX)
                
                // 4. Handles (Docked to the edges)
                handle(isLeft: true, x: leftX, trackWidth: trackWidth, duration: duration, totalWidth: width)
                handle(isLeft: false, x: rightX, trackWidth: trackWidth, duration: duration, totalWidth: width)

                // 5. Playhead
                let playheadRatio = Double(state.waveform.playbackPositionMillis) / (Double(state.waveform.durationMillis) > 0 ? Double(state.waveform.durationMillis) : 1.0)
                Rectangle()
                    .fill(designSystem.color(.recorderSecondary))
                    .frame(width: 2)
                    .offset(x: sidePadding + trackWidth * playheadRatio)
                    .shadow(radius: 1)
            }
            .coordinateSpace(name: "trimTrack")
        }
        .frame(height: 60)
        .padding(.vertical, 4)
    }
    
    private func miniWaveformCanvas(size: CGSize) -> some View {
        Canvas { context, size in
            let barStep: CGFloat = 2.5
            let count = Int(size.width / barStep)
            if !state.waveform.amplitudes.isEmpty {
                for i in 0..<count {
                    let x = CGFloat(i) * barStep
                    let ampIdx = Int(CGFloat(i) / CGFloat(count) * CGFloat(state.waveform.amplitudes.count))
                    let amp = state.waveform.amplitudes[min(ampIdx, state.waveform.amplitudes.count - 1)].floatValue
                    let h = max(3, CGFloat(amp) * size.height * 0.5)
                    let rect = CGRect(x: x, y: size.height/2 - h/2, width: 1.5, height: h)
                    context.fill(Path(roundedRect: rect, cornerRadius: 0.7), with: .color(designSystem.color(.textPrimary)))
                }
            }
        }
    }
    
    @ViewBuilder
    private func handle(isLeft: Bool, x: CGFloat, trackWidth: CGFloat, duration: Double, totalWidth: CGFloat) -> some View {
        let sidePadding: CGFloat = 12
        let handleWidth: CGFloat = 32 // Made it wider as requested
        
        VoiceRecorderTrimHandle(
            isLeft: isLeft, 
            designSystem: designSystem, 
            color: designSystem.color(.recorderAccent), 
            icon: isLeft ? state.trim.handleIconLeft : state.trim.handleIconRight
        )
        // Position handle so its INNER edge is exactly at 'x'. Zero gap.
        .position(x: isLeft ? x - (handleWidth / 2) : x + (handleWidth / 2), y: 30)
        .gesture(
            DragGesture(minimumDistance: 0, coordinateSpace: .named("trimTrack"))
                .onChanged { value in
                    isDragging = true
                    let newX = value.location.x
                    // Adjust to the inner edge of the wider handle
                    let adjustedX = isLeft ? newX + (handleWidth / 2) : newX - (handleWidth / 2)
                    let normalizedX = (adjustedX - sidePadding) / trackWidth
                    let newTime = max(0, min(normalizedX * duration, isLeft ? trimEnd - 0.1 : duration))
                    
                    if isLeft {
                        trimStart = min(newTime, trimEnd - 0.1)
                    } else {
                        trimEnd = max(newTime, trimStart + 0.1)
                    }
                    onAction(VoiceRecorderAction.UpdateTrimRange(start: Int64(trimStart * 1000), end: Int64(trimEnd * 1000)))
                }
                .onEnded { _ in 
                    isDragging = false 
                }
        )
    }
}




private struct VoiceRecorderTrimHandle: View {
    let isLeft: Bool
    let designSystem: DesignSystem
    let color: Color
    let icon: IconToken
    
    var body: some View {
        ZStack {
            // The Yellow Handle Block (Wider: 32pt)
            UnevenRoundedRectangle(
                topLeadingRadius: isLeft ? 12 : 0,
                bottomLeadingRadius: isLeft ? 12 : 0,
                bottomTrailingRadius: isLeft ? 0 : 12,
                topTrailingRadius: isLeft ? 0 : 12
            )
            .fill(color)
            .frame(width: 32, height: 60)
            .overlay(
                UnevenRoundedRectangle(
                    topLeadingRadius: isLeft ? 12 : 0,
                    bottomLeadingRadius: isLeft ? 12 : 0,
                    bottomTrailingRadius: isLeft ? 0 : 12,
                    topTrailingRadius: isLeft ? 0 : 12
                )
                .stroke(Color.white.opacity(0.2), lineWidth: 1)
            )
            
            Image(systemName: designSystem.icon(token: icon))
                .font(.system(size: 14, weight: .black))
                .foregroundColor(.white)
        }
        .contentShape(Rectangle())
        .frame(width: 48, height: 60)
    }
}
