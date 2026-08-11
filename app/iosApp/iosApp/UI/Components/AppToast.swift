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
                    .foregroundColor(designSystem.color(.textPrimary))
                
                Spacer()
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .background(
                designSystem.color(.glassBase)
                    .background(.ultraThinMaterial)
            )
            .cornerRadius(designSystem.dimen(.cornerRadius))
            .overlay(
                RoundedRectangle(cornerRadius: designSystem.dimen(.cornerRadius))
                    .stroke(designSystem.color(.glassBorder), lineWidth: 0.5)
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
            return designSystem.string(token)
        }
        return state.messageRaw ?? ""
    }

    private func getAccentColor() -> Color {
        switch state.type {
        case .success: return designSystem.color(.truth)
        case .error: return designSystem.color(.stress)
        case .warning: return designSystem.color(.accentPrimary)
        }
    }
}
