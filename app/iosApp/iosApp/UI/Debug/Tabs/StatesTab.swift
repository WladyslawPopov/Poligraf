import SwiftUI
import SharedLogic

struct StatesTab: View {
    let component: DebugComponent
    let designSystem: DesignSystem
    
    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                DebugSection(title: "Engine States", designSystem: designSystem) {
                    DebugActionButton(
                        text: designSystem.string(token: .debugTriggerLoading),
                        color: .accentPrimary,
                        designSystem: designSystem,
                        action: {
                           component.onAction(action: DebugAction.TriggerLoading())
                        }
                    )
                    
                    DebugActionButton(
                        text: designSystem.string(token: .debugTriggerErrorBlocking),
                        color: .stress,
                        designSystem: designSystem,
                        action: { component.onAction(action: DebugAction.TriggerErrorBlocking()) }
                    )
                }
                
                DebugSection(title: "Notifications / Toasts", designSystem: designSystem) {
                    DebugActionButton(
                        text: designSystem.string(token: .debugTriggerErrorToast),
                        color: .error,
                        designSystem: designSystem,
                        action: { component.onAction(action: DebugAction.TriggerErrorNonBlocking()) }
                    )
                    
                    DebugActionButton(
                        text: designSystem.string(token: .debugTriggerSuccessToast),
                        color: .truth,
                        designSystem: designSystem,
                        action: { component.onAction(action: DebugAction.TriggerSuccessToast()) }
                    )
                }
            }
            .padding(CGFloat(designSystem.dimen(token: .spacingMedium)))
        }
    }
}

struct DebugSection<Content: View>: View {
    let title: String
    let designSystem: DesignSystem
    let content: Content
    
    init(title: String, designSystem: DesignSystem, @ViewBuilder content: () -> Content) {
        self.title = title
        self.designSystem = designSystem
        self.content = content()
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(title.uppercased())
                .font(.caption)
                .fontWeight(.bold)
                .foregroundColor(IosTheme.color(.textSecondary, from: designSystem))
            
            VStack(spacing: 10) {
                content
            }
        }
        .padding(16)
        .background(IosTheme.color(.surfaceVariant, from: designSystem).opacity(0.4))
        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
    }
}

struct DebugActionButton: View {
    let text: String
    let color: ColorToken
    let designSystem: DesignSystem
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            Text(text)
                .font(.subheadline)
                .fontWeight(.semibold)
                .foregroundColor(IosTheme.color(.textInverted, from: designSystem))
                .frame(maxWidth: .infinity)
                .padding(.vertical, 12)
                .background(IosTheme.color(color, from: designSystem).opacity(0.8))
                .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
        }
    }
}
