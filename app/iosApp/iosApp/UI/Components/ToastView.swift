import SwiftUI
import SharedLogic

struct ToastView: View {
    let state: ToastState
    let designSystem: DesignSystem
    let onDismiss: () -> Void

    var body: some View {
        VStack {
            HStack {
                Text(getMessage())
                    .font(.subheadline)
                    .foregroundColor(getTextColor())
                Spacer()
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 12)
            .background(getBgColor())
            .cornerRadius(12)
            .padding(.horizontal, 16)
            .shadow(radius: 4)
            
            Spacer()
        }
        .padding(.top, 4)
        .transition(.move(edge: .top).combined(with: .opacity))
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

    private func getBgColor() -> Color {
        switch state.type {
        case .success: return IosTheme.color(.truth, from: designSystem)
        case .error: return IosTheme.color(.stress, from: designSystem)
        case .warning: return IosTheme.color(.primary, from: designSystem)
        default: return .gray
        }
    }

    private func getTextColor() -> Color {
        switch state.type {
        case .success, .error: return IosTheme.color(.textInverted, from: designSystem)
        case .warning: return IosTheme.color(.textPrimary, from: designSystem)
        default: return .white
        }
    }
}
