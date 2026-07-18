import SwiftUI
import SharedLogic
import CoreMotion

struct ScalesView: View {
    let designSystem: DesignSystem
    @State private var tiltX: Double = 0
    @State private var tiltY: Double = 0
    private let motionManager = CMMotionManager()
    
    var body: some View {
        TimelineView(.animation) { timeline in
            let time = timeline.date.timeIntervalSinceReferenceDate.remainder(dividingBy: 8.0) * (Double.pi * 2 / 8.0)
            
            ZStack {
                IosTheme.color(.background, from: designSystem)
                    .ignoresSafeArea()
                
                Canvas { context, size in
                    let rows = 36
                    let cols = 18
                    let cellWidth = size.width / CGFloat(cols)
                    let cellHeight = size.height / CGFloat(rows)
                    
                    // Parallax shift based on motion intensity from Design System
                    let intensity = Double(designSystem.dimen(token: .parallaxIntensity))
                    let parallaxX = tiltX * intensity
                    let parallaxY = tiltY * intensity
                    
                    let cornerRadius = CGFloat(designSystem.dimen(token: .cornerRadius))
                    let energyColor = IosTheme.color(.accentEnergy, from: designSystem)
                    let variantColor = IosTheme.color(.surfaceVariant, from: designSystem)
                    
                    for r in 0..<rows {
                        for c in 0..<cols {
                            let basePosX = CGFloat(c) * cellWidth + cellWidth / 2 + CGFloat(parallaxX)
                            let basePosY = CGFloat(r) * cellHeight + cellHeight / 2 + CGFloat(parallaxY)
                            
                            let dxNorm = abs(basePosX - size.width / 2) / (size.width * 0.5)
                            let dyNorm = abs(basePosY - size.height / 2) / (size.height * 0.5)
                            let distMask = pow(pow(dxNorm, 4) + pow(dyNorm, 4), 0.25)
                            
                            let rect = CGRect(x: basePosX - cellWidth/3, y: basePosY - cellHeight/4, width: cellWidth/1.5, height: cellHeight/2)
                            let path = RoundedRectangle(cornerRadius: cornerRadius).path(in: rect)
                            
                            // Concrete Base
                            context.opacity = 0.3
                            context.fill(path, with: .color(variantColor))
                            
                            // Energy Glow
                            let wave = sin(distMask * 10.0 - time)
                            let energyIntensity = max(0.2, min(1.0, distMask * 1.2))
                            let alpha = energyIntensity * 0.3 * (0.3 + max(0, wave) * 0.7)
                            
                            context.opacity = alpha
                            context.fill(path, with: .color(energyColor))
                        }
                    }
                }
                .blur(radius: 3) // Reduced blur to keep texture visible under glass
            }
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
