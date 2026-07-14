import SwiftUI
import SharedLogic

struct WidgetView: View {
    let widget: WidgetDto
    let designSystem: DesignSystem
    let onAction: (WidgetAction) -> Void
    
    var body: some View {
        Group {
            if let header = widget as? WidgetDto.Header {
                // 1. NATIVE LIQUID GLASS HEADER
                VStack(alignment: .center, spacing: 8) {
                    Text(designSystem.string(key: header.titleKey))
                        .font(.title2)
                        .fontWeight(.bold)
                        .foregroundColor(.white)
                        .multilineTextAlignment(.center)
                    
                    if let subtitle = header.subtitleKey {
                        Text(designSystem.string(key: subtitle))
                            .font(.subheadline)
                            .foregroundColor(.white.opacity(0.7))
                            .multilineTextAlignment(.center)
                    }
                }
                .padding(28)
                .frame(maxWidth: .infinity)
                // Use the built-in system material for real refraction
                .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 30, style: .continuous))
                .overlay(
                    RoundedRectangle(cornerRadius: 30, style: .continuous)
                        .stroke(.white.opacity(0.15), lineWidth: 0.5)
                )
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                
            } else if let micBtn = widget as? WidgetDto.MicrophoneButton {
                // 2. NATIVE GLASS SPHERE
                Button(action: { onAction(micBtn.action) }) {
                    ZStack {
                        Circle()
                            .fill(.ultraThinMaterial)
                            .frame(width: 96, height: 96)
                            .overlay(Circle().stroke(.white.opacity(0.3), lineWidth: 0.5))
                        
                        Image(systemName: "mic.fill")
                            .font(.system(size: 38))
                            .foregroundColor(.white)
                    }
                }
                .padding(.vertical, 30)
                
            } else if let stdBtn = widget as? WidgetDto.StandardButton {
                // 3. NATIVE GLASS BUTTON
                Button(action: { onAction(stdBtn.action) }) {
                    Text(designSystem.string(key: stdBtn.textKey))
                        .font(.headline)
                        .fontWeight(.bold)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 18)
                        .background(.thinMaterial, in: RoundedRectangle(cornerRadius: 22, style: .continuous))
                        .foregroundColor(.white)
                        .overlay(
                            RoundedRectangle(cornerRadius: 22, style: .continuous)
                                .stroke(.white.opacity(0.2), lineWidth: 0.5)
                        )
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 6)

            } else {
                Text("Unknown widget").foregroundColor(.white.opacity(0.4))
            }
        }
    }
}
