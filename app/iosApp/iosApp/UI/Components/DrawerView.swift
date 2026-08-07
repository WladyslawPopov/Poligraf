import SwiftUI
import SharedLogic

struct DrawerView: View {
    @Environment(\.horizontalSizeClass) var sizeClass
    @ObservedObject var navigator: IosNavigator
    let designSystem: DesignSystem
    let appConfig: AppConfig?
    let onUserClose: () -> Void 
    
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Header Area with Accent Background (matching Android)
            ZStack(alignment: .bottomLeading) {
                LinearGradient(
                    colors: [designSystem.color(.accentPrimary).opacity(0.15), .clear],
                    startPoint: .top,
                    endPoint: .bottom
                )
                .frame(height: sizeClass == .compact ? 120 : 160)
                
                VStack(alignment: .leading, spacing: 0) {
                    HStack {
                        Text(designSystem.string(token: .drawerSettings))
                            .font(.system(size: 28, weight: .heavy))
                            .foregroundColor(designSystem.color(.accentPrimary))
                        
                        Spacer()
                        
                        if sizeClass != .compact {
                            Button(action: onUserClose) {
                                Image(systemName: designSystem.icon(.close))
                                    .font(.system(size: 18, weight: .bold))
                                    .foregroundColor(designSystem.color(.textSecondary))
                            }
                        }
                    }
                    .padding(.horizontal, designSystem.dimen(.spacingLarge))
                    .padding(.bottom, 24)
                    
                    // Glass Divider
                    Rectangle()
                        .fill(
                            LinearGradient(
                                colors: [.clear, designSystem.color(.accentPrimary).opacity(0.3), .clear],
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        )
                        .frame(height: 1)
                        .padding(.horizontal, designSystem.dimen(.spacingLarge))
                }
            }
            .ignoresSafeArea(edges: .top)
            
            ScrollView {
                VStack(alignment: .leading, spacing: designSystem.dimen(.spacingLarge)) {
                    // Dark Mode Toggle
                    HStack {
                        Text(designSystem.string(token: .drawerDarkMode))
                            .font(.headline)
                            .foregroundColor(designSystem.color(.textPrimary))
                        
                        Spacer()
                        
                        Toggle("", isOn: Binding(
                            get: { navigator.isDark },
                            set: { _ in navigator.toggleTheme() }
                        ))
                        .tint(designSystem.color(.accentPrimary))
                        .labelsHidden()
                    }
                    .padding()
                    .clipShape(RoundedRectangle(cornerRadius: 16))
                    
                    if designSystem.isDebug {
                        // Glass Divider
                        Rectangle()
                            .frame(height: 1)
                        
                        Button(action: {
                            onUserClose()
                            navigator.openDebug()
                        }) {
                            HStack {
                                Text(designSystem.string(token: .openDebugSandbox))
                                    .font(.headline)
                                    .foregroundColor(designSystem.color(.accentEnergy))
                                Spacer()
                                Image(systemName: "chevron.right")
                                    .font(.caption)
                                    .foregroundColor(designSystem.color(.accentEnergy))
                            }
                            .padding()
                            .clipShape(RoundedRectangle(cornerRadius: 16))
                        }
                    }
                }
                .padding(designSystem.dimen(.mainPadding))
            }

            Spacer()

            // Footer Info
            VStack(alignment: .leading, spacing: 4) {
                Text(designSystem.string(token: .drawerFooterTitle).uppercased())
                    .font(.system(size: 12, weight: .black))
                    .foregroundColor(designSystem.color(.textPrimary))
                
                HStack(spacing: 4) {
                    Text(designSystem.string(token: .drawerFooterSubtitle).uppercased())
                    Text("\(appConfig?.appVersion ?? "1.0.0") (BUILD 1)")
                }
                .font(.system(size: 10, weight: .bold))
                .foregroundColor(designSystem.color(.accentPrimary).opacity(0.8))
            }
            .padding(.horizontal, designSystem.dimen(.spacingLarge))
            .padding(.bottom, 40)
        }
        .background(.clear)
    }
}
