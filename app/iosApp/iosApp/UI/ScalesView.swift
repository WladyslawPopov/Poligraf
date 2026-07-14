import SwiftUI
import SharedLogic

struct ScalesView: View {
    @ObservedObject var visualizer: ObservableState<BackgroundState>
    let designSystem: DesignSystem
    
    var body: some View {
        TimelineView(.animation) { timeline in
            let time = timeline.date.timeIntervalSinceReferenceDate.remainder(dividingBy: 6.0) * (Double.pi * 2 / 6.0)
            
            ZStack {
                IosTheme.color(.background, from: designSystem)
                    .ignoresSafeArea()
                
                Canvas { context, size in
                    let rows = 36
                    let cols = 18
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
                            let ox = tx * 5.0
                            let oy = ty * 5.0
                            let drawCenterX = basePosX + CGFloat(ox)
                            let drawCenterY = basePosY + CGFloat(oy)
                            
                            let rawWave = sin(distMask * 8.0 - time)
                            let wave = max(0, min(1.0, rawWave))
                            let energyIntensity = max(0.25, min(1.0, distMask * 1.1))
                            
                            var subContext = context
                            subContext.translateBy(x: drawCenterX, y: drawCenterY)
                            subContext.rotate(by: rotation)
                            
                            let rect = CGRect(x: -cellWidth/3, y: -cellHeight/4, width: cellWidth/1.5, height: cellHeight/2)
                            let path = RoundedRectangle(cornerRadius: 14).path(in: rect)
                            
                            subContext.opacity = 0.5
                            subContext.fill(path, with: .color(IosTheme.color(.surfaceVariant, from: designSystem)))
                            
                            let alpha = energyIntensity * 0.4 * (0.4 + wave * 0.6)
                            subContext.opacity = alpha
                            subContext.fill(path, with: .color(Color(hex: "#00F2FF")))
                        }
                    }
                }
            }
        }
    }
}
