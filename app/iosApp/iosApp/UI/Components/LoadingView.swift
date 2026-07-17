import SwiftUI
import SharedLogic

struct LoadingView: View {
    let isVisible: Bool
    let designSystem: DesignSystem

    var body: some View {
        if isVisible {
            VStack {
                Spacer().frame(height: 16)
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: IosTheme.color(.accentEnergy, from: designSystem)))
                    .padding(10)
                    .background(.ultraThinMaterial)
                    .clipShape(Circle())
                Spacer()
            }
            .transition(.move(edge: .top).combined(with: .opacity))
            .animation(.spring(), value: isVisible)
        }
    }
}
