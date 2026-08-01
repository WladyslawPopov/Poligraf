import SwiftUI
import SharedLogic
import CoreMotion

struct ScalesView: View {
    let designSystem: DesignSystem
    let config: AppBackground.AnimatedScales
    
    @State private var tiltX: Double = 0
    @State private var tiltY: Double = 0
    private let motionManager = CMMotionManager()
    
    var body: some View {
        TimelineView(.animation) { timeline in
            let speed = Double(config.animationSpeed)
            let time = timeline.date.timeIntervalSinceReferenceDate.remainder(dividingBy: 8.0 / speed) * (Double.pi * 2 / (8.0 / speed))
            
            let cellWidth = CGFloat(designSystem.dimen(token: .backgroundCellWidth))
            let cellHeight = CGFloat(designSystem.dimen(token: .backgroundCellHeight))
            
            ZStack {
                IosTheme.color(config.baseColor, from: designSystem)
                    .ignoresSafeArea()
                
                Canvas { context, size in
                    let cols = Int(size.width / cellWidth)
                    let rows = Int(size.height / cellHeight)
                    
                    let cellW = size.width / CGFloat(cols)
                    let cellH = size.height / CGFloat(rows)
                    
                    let intensity = Double(designSystem.dimen(token: .parallaxIntensity)) * Double(config.parallaxIntensity)
                    let px = CGFloat(tiltX * intensity)
                    let py = CGFloat(tiltY * intensity)
                    
                    let cornerRadius = CGFloat(designSystem.dimen(token: .cornerRadius))
                    let energyColor = IosTheme.color(config.energyColor, from: designSystem)
                    let variantColor = IosTheme.color(config.particleColor, from: designSystem)
                    
                    // Expanded range (-2 to +2) to ensure no edge gaps during parallax
                    for r in -2...rows + 2 {
                        let y = CGFloat(r) * cellH + cellH / 2 + py
                        
                        for c in -2...cols + 2 {
                            let x = CGFloat(c) * cellW + cellW / 2 + px
                            
                            let dxNorm = abs(x - size.width / 2) / (size.width * 0.5)
                            let dyNorm = abs(y - size.height / 2) / (size.height * 0.5)
                            let distMask = pow(pow(dxNorm, 4) + pow(dyNorm, 4), 0.25)
                            
                            let rect = CGRect(x: x - cellW/3, y: y - cellH/4, width: cellW/1.5, height: cellH/2)
                            let path = RoundedRectangle(cornerRadius: cornerRadius).path(in: rect)
                            
                            // 1. Draw Scale (Sharp)
                            context.opacity = 0.3
                            context.fill(path, with: .color(variantColor))
                            
                            // 2. Draw Energy (Sharp)
                            let wave = sin(distMask * 10.0 - time)
                            let energyIntensity = max(0.2, min(1.0, distMask * 1.2))
                            let alpha = energyIntensity * 0.4 * (0.3 + max(0, wave) * 0.7)
                            
                            context.opacity = alpha
                            context.fill(path, with: .color(energyColor))
                        }
                    }
                }
                .blur(radius: CGFloat(config.blurRadius))
            }
            .ignoresSafeArea()
        }
        .onAppear {
            if motionManager.isDeviceMotionAvailable {
                motionManager.deviceMotionUpdateInterval = 1.0 / 60.0
                motionManager.startDeviceMotionUpdates(to: .main) { motion, _ in
                    if let gravity = motion?.gravity {
                        withAnimation(.spring(response: 0.5, dampingFraction: 0.8)) {
                            self.tiltX = gravity.x
                            self.tiltY = -gravity.y
                        }
                    }
                }
            }
        }
        .onDisappear {
            motionManager.stopDeviceMotionUpdates()
        }
    }
}
