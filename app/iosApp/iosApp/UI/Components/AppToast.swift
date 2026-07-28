import SwiftUI
import SharedLogic

struct AppToast: View {
    let state: ToastState
    let designSystem: DesignSystem
    let onDismiss: () -> Void

    var body: some View {
        VStack {
            Spacer()
            
            HStack(spacing: 12) {
                // Semantic Neon Indicator
                Circle()
                    .fill(getAccentColor())
                    .frame(width: 8, height: 8)
                
                Text(getMessage())
                    .font(.subheadline)
                    .foregroundColor(IosTheme.color(.textPrimary, from: designSystem))
                
                Spacer()
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .background(
                IosTheme.color(.glassBase, from: designSystem)
                    .background(.ultraThinMaterial)
            )
            .cornerRadius(CGFloat(designSystem.dimen(token: .cornerRadius)))
            .overlay(
                RoundedRectangle(cornerRadius: CGFloat(designSystem.dimen(token: .cornerRadius)))
                    .stroke(IosTheme.color(.glassBorder, from: designSystem), lineWidth: 0.5)
            )
            .padding(.horizontal, 16)
        }
        .padding(.bottom, 24)
        .transition(.move(edge: .bottom).combined(with: .opacity))
        .onAppear {
            DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
                onDismiss()
            }
        }
    }

    private func getMessage() -> String {
        if let token = state.messageToken {
            return designSystem.string(token: token)
        }
        return state.messageRaw ?? ""
    }

    private func getAccentColor() -> Color {
        switch state.type {
        case .success: return IosTheme.color(.truth, from: designSystem)
        case .error: return IosTheme.color(.stress, from: designSystem)
        case .warning: return IosTheme.color(.accentPrimary, from: designSystem)
        default: return .gray
        }
    }
}
