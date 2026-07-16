import SwiftUI
import SharedLogic

struct DrawerView: View {
    @ObservedObject var root: RootComponentWrapper
    let designSystem: DesignSystem
    
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Spacer().frame(height: 80)
            
            Text("Settings")
                .font(.title2)
                .bold()
                .padding(.horizontal)
                .foregroundColor(IosTheme.color(.textPrimary, from: designSystem))

            Toggle("Dark Mode", isOn: Binding(
                get: { root.isDark },
                set: { _ in root.toggleTheme() }
            ))
            .padding()
            .foregroundColor(IosTheme.color(.textPrimary, from: designSystem))

            Spacer()
        }
        .background(IosTheme.color(.surface, from: designSystem))
    }
}
