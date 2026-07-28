import SwiftUI
import SharedLogic

struct StatesTab: View {
    let component: DebugComponent
    let designSystem: DesignSystem
    
    var body: some View {
        VStack(spacing: CGFloat(designSystem.dimen(token: .widgetSpacing))) {
            Button(action: { component.onAction(action: .debugTriggerLoading) }) {
                Text(designSystem.string(token: .debugTriggerLoading))
                    .foregroundColor(IosTheme.color(.textInverted, from: designSystem))
            }
            .buttonStyle(.borderedProminent)
            
            Button(action: { component.onAction(action: .debugTriggerErrorBlocking) }) {
                Text(designSystem.string(token: .debugTriggerErrorBlocking))
                    .foregroundColor(IosTheme.color(.textInverted, from: designSystem))
            }
            .buttonStyle(.borderedProminent)
            
            Button(action: { component.onAction(action: .debugTriggerErrorNonBlocking) }) {
                Text(designSystem.string(token: .debugTriggerErrorToast))
                    .foregroundColor(IosTheme.color(.textInverted, from: designSystem))
            }
            .buttonStyle(.borderedProminent)
            
            Button(action: { component.onAction(action: .debugTriggerSuccessToast) }) {
                Text(designSystem.string(token: .debugTriggerSuccessToast))
                    .foregroundColor(IosTheme.color(.textInverted, from: designSystem))
            }
            .buttonStyle(.borderedProminent)
            
            Spacer()
        }
        .padding(CGFloat(designSystem.dimen(token: .mainPadding)))
        .tint(IosTheme.color(.accentPrimary, from: designSystem))
    }
}
