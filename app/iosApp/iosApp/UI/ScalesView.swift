import SwiftUI
import SharedLogic

struct ScalesView: View {
    @ObservedObject var visualizer: ObservableState<BackgroundState>
    let designSystem: DesignSystem
    
    var body: some View {
        TimelineView(.animation) { timeline in
            let time = timeline.date.timeIntervalSinceReferenceDate.remainder(dividingBy: 6.0) * (Double.pi * 2 / 6.0)
            
            ZStack {
                // ALWAYS use our design system background (Deep Anthracite)
                IosTheme.color(.background, from: designSystem)
                    .ignoresSafeArea()
                
                Canvas { context, size in
                    let rows = 38
                    let cols = 19
                    let cellWidth = size.width / CGFloat(cols)
                    let cellHeight = size.height / CGFloat(rows)
                    
                    let tx = Double(visualizer.value.tiltX)
                    let ty = Double(visualizer.value.tiltY)
                    
                    for r in 0..<rows {
                        for c in 0..<cols {
                            let basePosX = CGFloat(c) * cellWidth + cellWidth / 2
                            let basePosY = CGFloat(r) * cellHeight + cellHeight / 2
                            
                            let dxNorm = abs(basePosX - size.width / 2) / (size.width * 0.45)
                            let dyNorm = abs(basePosY - size.height / 2) / (size.height * 0.45)
                            let distMask = pow(pow(dxNorm, 4) + pow(dyNorm, 4), 0.25)
                            
                            let rotation = Angle(degrees: (tx + ty) * 20.0)
                            let energyIntensity = max(0.15, min(1.0, distMask * 1.2 - 0.1))
                            
                            var subContext = context
                            subContext.translateBy(x: basePosX + CGFloat(tx * 4.0), y: basePosY + CGFloat(ty * 4.0))
                            subContext.rotate(by: rotation)
                            
                            let rect = CGRect(x: -cellWidth/3, y: -cellHeight/4, width: cellWidth/1.5, height: cellHeight/2)
                            let path = RoundedRectangle(cornerRadius: 16).path(in: rect)
                            
                            // Concrete Base: Always dark/gray as on Android
                            subContext.opacity = 0.35
                            subContext.fill(path, with: .color(IosTheme.color(.surfaceVariant, from: designSystem)))
                            
                            // Energy Glow (Cyan)
                            let wave = sin(distMask * 10.0 - time)
                            let alpha = energyIntensity * 0.35 * (0.3 + max(0, wave) * 0.7)
                            subContext.opacity = alpha
                            subContext.fill(path, with: .color(Color(hex: "#00F2FF")))
                        }
                    }
                }
            }
        }
    }
}
