import SwiftUI
import SharedLogic

struct VoiceRecorderProfessionalWaveformView: View {
    @ObservedObject var engine: NativeVoiceRecorderEngine
    let isTrimMode: Bool
    @Binding var trimStart: TimeInterval
    @Binding var trimEnd: TimeInterval
    let designSystem: DesignSystem
    let onSeek: (Double) -> Void
    
    // MARK: - Configuration
    private let step: CGFloat = 4          
    private let barWidth: CGFloat = 1.5    
    private let millisPerBar: Double = 33  
    private let rulerAreaHeight: CGFloat = 40 // Высота зоны с цифрами внизу
    
    // MARK: - State
    @State private var dragOffset: CGFloat = 0
    @State private var isDragging: Bool = false
    @State private var lastSeekTime: TimeInterval = 0

    var body: some View {
        GeometryReader { geometry in
            let size = geometry.size
            let waveformHeight = size.height - rulerAreaHeight
            let playheadX = engine.isRecording ? size.width * 0.85 : size.width * 0.5
            
            let visualTime: Double = {
                if isDragging {
                    let timeDelta = Double(-dragOffset / step * CGFloat(millisPerBar)) / 1000.0
                    return max(0, min(engine.duration, lastSeekTime + timeDelta))
                } else if engine.isRecording {
                    return Double(engine.amplitudes.count) * millisPerBar / 1000.0
                } else {
                    return engine.currentTime
                }
            }()

            VStack(spacing: 0) {
                // 1. Область волны (темная)
                ZStack(alignment: .leading) {
                    designSystem.color(.surface).opacity(0.6)
                    
                    Canvas { context, size in
                        let engineOffset = CGFloat(visualTime * 1000 / millisPerBar) * step
                        let startX = playheadX - engineOffset
                        
                        // 0. Trim Highlight Background (Glassy Glow)
                        if isTrimMode {
                            let startTrimX = startX + CGFloat(trimStart * 1000 / millisPerBar) * step
                            let endTrimX = startX + CGFloat(trimEnd * 1000 / millisPerBar) * step
                            let highlightRect = CGRect(x: startTrimX, y: 0, width: endTrimX - startTrimX, height: size.height)
                            
                            // 1. Soft Background Glow
                            context.fill(Path(highlightRect), with: .color(designSystem.color(.recorderAccent).opacity(0.1)))
                            
                            // 2. Subtle Vertical Borders (Inner Shadow effect)
                            var borderPath = Path()
                            borderPath.move(to: CGPoint(x: startTrimX, y: 0))
                            borderPath.addLine(to: CGPoint(x: startTrimX, y: size.height))
                            borderPath.move(to: CGPoint(x: endTrimX, y: 0))
                            borderPath.addLine(to: CGPoint(x: endTrimX, y: size.height))
                            context.stroke(borderPath, with: .color(designSystem.color(.recorderAccent).opacity(0.4)), lineWidth: 1.5)

                            // 3. Horizontal Guide Lines
                            var guidePath = Path()
                            guidePath.move(to: CGPoint(x: startTrimX, y: 0.75))
                            guidePath.addLine(to: CGPoint(x: endTrimX, y: 0.75))
                            guidePath.move(to: CGPoint(x: startTrimX, y: size.height - 0.75))
                            guidePath.addLine(to: CGPoint(x: endTrimX, y: size.height - 0.75))
                            context.stroke(guidePath, with: .color(designSystem.color(.recorderAccent).opacity(0.6)), lineWidth: 1.5)
                        }

                        let firstVisibleIdx = Int(floor((-startX - 50) / step))
                        let lastVisibleIdx = Int(ceil((size.width - startX + 50) / step))
                        
                        let amplitudes = engine.amplitudes
                        let safeStart = max(0, firstVisibleIdx)
                        let safeEnd = min(amplitudes.count, lastVisibleIdx)

                        // Отрисовка Волноформы
                        if safeStart < safeEnd {
                            for i in safeStart..<safeEnd {
                                let amp = amplitudes[i]
                                let x = startX + CGFloat(i) * step
                                
                                let h = max(2, CGFloat(amp) * size.height * 0.7)
                                let rect = CGRect(x: x - barWidth/2, y: (size.height - h)/2, width: barWidth, height: h)
                                
                                let barTime = Double(i) * millisPerBar / 1000.0
                                var color: Color
                                
                                if isTrimMode {
                                    color = (barTime >= trimStart && barTime <= trimEnd) 
                                        ? designSystem.color(.textPrimary) 
                                        : designSystem.color(.textPrimary).opacity(0.15)
                                } else {
                                    if engine.isRecording {
                                        color = designSystem.color(.recorderPrimary)
                                    } else {
                                        color = barTime < (visualTime - 0.01)
                                            ? designSystem.color(.textPrimary).opacity(0.9) 
                                            : designSystem.color(.textPrimary).opacity(0.2)
                                    }
                                }
                                context.fill(Path(roundedRect: rect, cornerRadius: 0.5), with: .color(color))
                            }
                        }
                        
                        // Штрихи линейки (внутри темной области по нижнему краю)
                        drawRulerTicks(in: &context, startX: startX, size: size)
                    }

                    // Синий плейхед (строго внутри темной области)
                    if !isTrimMode {
                        playheadView(isRecording: engine.isRecording)
                            .position(x: playheadX, y: waveformHeight / 2)
                    }
                }
                .frame(height: waveformHeight)
                
                // 2. Область подписей линейки (светлая/прозрачная)
                Canvas { context, size in
                    let engineOffset = CGFloat(visualTime * 1000 / millisPerBar) * step
                    let startX = playheadX - engineOffset
                    drawRulerLabels(in: &context, startX: startX, size: size)
                }
                .frame(height: rulerAreaHeight)
            }
            .contentShape(Rectangle())
            .gesture(
                DragGesture(minimumDistance: 0)
                    .onChanged { value in
                        if engine.isRecording { return }
                        if !isDragging {
                            isDragging = true
                            lastSeekTime = engine.currentTime
                            engine.pausePlayback()
                        }
                        dragOffset = value.translation.width
                        let timeDelta = Double(-dragOffset / step * CGFloat(millisPerBar)) / 1000.0
                        onSeek(max(0, min(engine.duration, lastSeekTime + timeDelta)))
                    }
                    .onEnded { _ in
                        isDragging = false
                        dragOffset = 0
                    }
            )
        }
    }
    
    private func playheadView(isRecording: Bool) -> some View {
        let color = isRecording ? designSystem.color(.recorderPrimary) : designSystem.color(.recorderSecondary)
        return ZStack {
            Rectangle()
                .fill(color)
                .frame(width: 1.5)
            VStack {
                Circle().fill(color).frame(width: 6, height: 6)
                Spacer()
                Circle().fill(color).frame(width: 6, height: 6)
            }
        }
    }

    private func drawRulerTicks(in context: inout GraphicsContext, startX: CGFloat, size: CGSize) {
        let secondInterval = CGFloat(1000 / millisPerBar) * step
        let subInterval = secondInterval / 10 
        let firstVisible = Int(max(0, floor(-startX / subInterval)))
        let lastVisible = Int(ceil((size.width - startX) / subInterval))
        
        // Линия-разделитель
        var linePath = Path()
        linePath.move(to: CGPoint(x: 0, y: size.height))
        linePath.addLine(to: CGPoint(x: size.width, y: size.height))
        context.stroke(linePath, with: .color(designSystem.color(.textPrimary).opacity(0.1)), lineWidth: 0.5)

        for i in firstVisible...lastVisible {
            let x = startX + CGFloat(i) * subInterval
            let isSecond = i % 10 == 0
            let h: CGFloat = isSecond ? 8 : 4
            
            var tickPath = Path()
            tickPath.move(to: CGPoint(x: x, y: size.height))
            tickPath.addLine(to: CGPoint(x: x, y: size.height - h))
            context.stroke(tickPath, with: .color(designSystem.color(.textPrimary).opacity(isSecond ? 0.3 : 0.1)), lineWidth: 0.5)
        }
    }

    private func drawRulerLabels(in context: inout GraphicsContext, startX: CGFloat, size: CGSize) {
        let secondInterval = CGFloat(1000 / millisPerBar) * step
        let firstVisible = Int(max(0, floor(-startX / secondInterval)))
        let lastVisible = Int(ceil((size.width - startX) / secondInterval))
        
        for s in firstVisible...lastVisible {
            let x = startX + CGFloat(s) * secondInterval
            let timeStr = String(format: "%d:%02d", s / 60, s % 60)
            context.draw(
                Text(timeStr)
                    .font(.system(size: 10, weight: .regular, design: .monospaced))
                    .foregroundColor(designSystem.color(.textPrimary).opacity(0.4)),
                at: CGPoint(x: x, y: 15)
            )
        }
    }
}
