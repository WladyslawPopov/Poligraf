import SwiftUI
import SharedLogic

struct ErrorView: View {
    let type: ErrorType
    let designSystem: DesignSystem
    let onRetry: () -> Void

    var body: some View {
        let titleToken = getTitleToken()
        let msgToken = getMsgToken()
        let emoji = getEmoji()

        ZStack {
            VStack(spacing: 24) {
                Text(emoji)
                    .font(.system(size: 64))

                VStack(spacing: 8) {
                    Text(designSystem.string(token: titleToken))
                        .font(.title2)
                        .bold()
                        .foregroundColor(designSystem.color(.textPrimary))

                    Text(designSystem.string(token: msgToken))
                        .font(.body)
                        .multilineTextAlignment(.center)
                        .foregroundColor(designSystem.color(.textSecondary))
                        .padding(.horizontal, 32)
                }

                Button(action: onRetry) {
                    Text(designSystem.string(token: .errorRetry))
                        .fontWeight(.bold)
                        .padding(.horizontal, 32)
                        .padding(.vertical, 12)
                        .background(designSystem.color(.accentEnergy))
                        .foregroundColor(designSystem.color(.textInverted))
                        .cornerRadius(12)
                }
            }
            .padding(.vertical, 40)
            .frame(maxWidth: .infinity)
            .background(designSystem.color(.glassBase).opacity(0.3))
            .background(.ultraThinMaterial)
            .cornerRadius(28)
            .padding(24)
        }
        .frame(maxHeight: .infinity)
    }

    private func getEmoji() -> String {
        switch type {
        case .noInternet: return "🌐"
        case .serverUnavailable: return "📡"
        default: return "⚠️"
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
