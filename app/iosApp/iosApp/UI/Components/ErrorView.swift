import SwiftUI
import SharedLogic

struct ErrorView: View {
    let type: ErrorType
    let designSystem: DesignSystem
    let onRetry: () -> Void
    let onDismiss: () -> Void

    var body: some View {
        let titleToken = getTitleToken()
        let msgToken = getMsgToken()

        if type == .unknown || type == .unauthorized {
            // Standard Alert for minor errors
            Text("") // Invisible anchor for alert
                .alert(isPresented: .constant(true)) {
                    Alert(
                        title: Text(designSystem.string(token: titleToken)),
                        message: Text(designSystem.string(token: msgToken)),
                        dismissButton: .default(Text(designSystem.string(token: .errorRetry)), action: onDismiss)
                    )
                }
        } else {
            // Full screen overlay for critical connectivity issues
            ZStack {
                IosTheme.color(.background, from: designSystem)
                    .ignoresSafeArea()

                VStack(spacing: 24) {
                    VStack(spacing: 8) {
                        Text(designSystem.string(token: titleToken))
                            .font(.title)
                            .bold()
                            .foregroundColor(IosTheme.color(.textPrimary, from: designSystem))

                        Text(designSystem.string(token: msgToken))
                            .font(.body)
                            .multilineTextAlignment(.center)
                            .foregroundColor(IosTheme.color(.textSecondary, from: designSystem))
                            .padding(.horizontal, 32)
                    }

                    Button(action: onRetry) {
                        Text(designSystem.string(token: .errorRetry))
                            .fontWeight(.bold)
                            .padding(.horizontal, 32)
                            .padding(.vertical, 12)
                            .background(IosTheme.color(.primary, from: designSystem))
                            .foregroundColor(IosTheme.color(.onPrimary, from: designSystem))
                            .cornerRadius(12)
                    }
                }
            }
        }
    }

    private func getTitleToken() -> StringToken {
        switch type {
        case .noInternet: return .errorNoInternetTitle
        case .serverUnavailable: return .errorServerTitle
        default: return .errorUnknownTitle
        }
    }

    private func getMsgToken() -> StringToken {
        switch type {
        case .noInternet: return .errorNoInternetMsg
        case .serverUnavailable: return .errorServerMsg
        default: return .errorUnknownMsg
        }
    }
}
