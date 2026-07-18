import SwiftUI
import SharedLogic

struct DrawerView: View {
    @ObservedObject var root: RootComponentWrapper
    let designSystem: DesignSystem
    
    var body: some View {
        ZStack {
            IosTheme.color(.background, from: designSystem)
                .ignoresSafeArea()
            
            VStack(alignment: .leading, spacing: 0) {
                Spacer().frame(height: 80)
                
                Text(designSystem.string(token: .drawerSettings))
                    .font(.title2)
                    .bold()
                    .padding(.horizontal)
                    .foregroundColor(IosTheme.color(.textPrimary, from: designSystem))

                Toggle(designSystem.string(token: .drawerDarkMode), isOn: Binding(
                    get: { root.isDark },
                    set: { _ in root.toggleTheme() }
                ))
                .padding()
                .foregroundColor(IosTheme.color(.textPrimary, from: designSystem))

                Spacer()
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
