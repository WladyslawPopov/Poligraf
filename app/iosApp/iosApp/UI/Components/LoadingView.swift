import SwiftUI
import SharedLogic

struct LoadingView: View {
    let isVisible: Bool
    let designSystem: DesignSystem

    var body: some View {
        if isVisible {
            VStack {
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: designSystem.color(.accentEnergy)))
                    .padding(10)
                    .background(.ultraThinMaterial)
                    .clipShape(Circle())
                Spacer()
            }
            .padding(.top, 8)
            .transition(.move(edge: .top).combined(with: .opacity))
            .animation(.spring(), value: isVisible)
        }
    }
}
