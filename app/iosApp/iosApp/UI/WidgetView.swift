import SwiftUI
import SharedLogic

struct WidgetView: View {
    let widget: WidgetDto
    let designSystem: DesignSystem
    let onAction: (WidgetAction) -> Void
    
    var body: some View {
        Group {
            // Check types based on KMP naming convention for Swift
            if let header = widget as? WidgetDto.Header {
                VStack(alignment: .center, spacing: 8) {
                    Text(designSystem.string(key: header.titleKey))
                        .font(.title)
                        .foregroundColor(IosTheme.color(.textPrimary, from: designSystem))
                    
                    if let subtitle = header.subtitleKey {
                        Text(designSystem.string(key: subtitle))
                            .font(.body)
                            .foregroundColor(IosTheme.color(.textSecondary, from: designSystem))
                    }
                }
                .frame(maxWidth: .infinity)
                .padding()
                
            } else if let micBtn = widget as? WidgetDto.MicrophoneButton {
                Button(action: { onAction(micBtn.action) }) {
                    ZStack {
                        Circle()
                            .fill(IosTheme.color(.primary, from: designSystem))
                            .frame(width: 80, height: 80)
                        
                        Image(systemName: "mic.fill")
                            .font(.system(size: 32))
                            .foregroundColor(IosTheme.color(.onPrimary, from: designSystem))
                    }
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 32)
                
            } else if let stdBtn = widget as? WidgetDto.StandardButton {
                Button(action: { onAction(stdBtn.action) }) {
                    Text(designSystem.string(key: stdBtn.textKey))
                        .fontWeight(.bold)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(stdBtn.isPrimary ? IosTheme.color(.primary, from: designSystem) : Color.clear)
                        .foregroundColor(stdBtn.isPrimary ? IosTheme.color(.onPrimary, from: designSystem) : IosTheme.color(.primary, from: designSystem))
                        .cornerRadius(12)
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(IosTheme.color(.primary, from: designSystem), lineWidth: stdBtn.isPrimary ? 0 : 2)
                        )
                }
                .padding(.horizontal, 32)
                .padding(.vertical, 16)

            } else {
                Text("Unknown widget")
            }
        }
        .listRowBackground(Color.clear)
    }
}
