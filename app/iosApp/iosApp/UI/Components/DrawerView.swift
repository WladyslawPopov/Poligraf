import SwiftUI
import SharedLogic

struct DrawerView: View {
    @ObservedObject var navigator: IosNavigator
    let designSystem: DesignSystem
    let appConfig: AppConfig?
    let onUserClose: () -> Void 
    
    var body: some View {
        AppSheetContainer(
            designSystem: designSystem,
            titleToken: .drawerSettings,
            onUserClose: onUserClose
        ) {
            VStack(alignment: .leading, spacing: designSystem.dimen(token: .spacingLarge)) {
                // Dark Mode Toggle
                HStack {
                    Text(designSystem.string(token: .drawerDarkMode))
                        .font(.headline)
                        .foregroundColor(designSystem.color(token: .textPrimary))
                    
                    Spacer()
                    
                    Toggle("", isOn: Binding(
                        get: { navigator.isDark },
                        set: { _ in navigator.toggleTheme() }
                    ))
                    .tint(designSystem.color(token: .accentPrimary))
                    .labelsHidden()
                }
                .padding()
                .background(designSystem.color(token: .glassBase).opacity(0.1))
                .clipShape(RoundedRectangle(cornerRadius: 16))
                
                if designSystem.isDebug {
                    // Glass Divider
                    Rectangle()
                        .fill(designSystem.color(token: .glassBorder).opacity(0.2))
                        .frame(height: 1)
                    
                    Button(action: {
                        onUserClose()
                        navigator.openDebug()
                    }) {
                        HStack {
                            Text(designSystem.string(token: .openDebugSandbox))
                                .font(.headline)
                                .foregroundColor(designSystem.color(token: .accentEnergy))
                            Spacer()
                            Image(systemName: "chevron.right")
                                .font(.caption)
                                .foregroundColor(designSystem.color(token: .accentEnergy))
                        }
                        .padding()
                        .background(designSystem.color(token: .glassBase).opacity(0.1))
                        .clipShape(RoundedRectangle(cornerRadius: 16))
                    }
                }
                
                Spacer()

                // Footer Info
                VStack(alignment: .leading, spacing: 4) {
                    Text(designSystem.string(token: .drawerFooterTitle).uppercased())
                        .font(.system(size: 12, weight: .black))
                        .foregroundColor(designSystem.color(token: .textPrimary))
                    
                    HStack(spacing: 4) {
                        Text(designSystem.string(token: .drawerFooterSubtitle).uppercased())
                        Text("\(appConfig?.appVersion ?? "1.0.0") (BUILD 1)")
                    }
                    .font(.system(size: 10, weight: .bold))
                    .foregroundColor(designSystem.color(token: .accentPrimary).opacity(0.8))
                }
                .padding(.bottom, 40)
            }
            .padding(designSystem.dimen(token: .mainPadding))
        }
    }
}
