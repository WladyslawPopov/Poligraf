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
            // Header
            HStack {
                Text(designSystem.string(token: .drawerSettings))
                    .font(.title2)
                    .bold()
                    .foregroundColor(IosTheme.color(.textPrimary, from: designSystem))
                
                Spacer()
                
                if sizeClass != .compact {
                    Button(action: onUserClose) {
                        Image(systemName: designSystem.icon(token: .close))
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(IosTheme.color(.textSecondary, from: designSystem))
                    }
                }
            }
            .padding(.top, sizeClass == .compact ? CGFloat(truncating: designSystem.dimen(token: .paddingError) as NSNumber) : CGFloat(truncating: designSystem.dimen(token: .headerHeight) as NSNumber))
            .padding(.horizontal, CGFloat(truncating: designSystem.dimen(token: .mainPadding) as NSNumber))
            
            VStack(alignment: .leading, spacing: CGFloat(truncating: designSystem.dimen(token: .spacingLarge) as NSNumber)) {
                HStack {
                    Text(designSystem.string(token: .drawerDarkMode))
                        .foregroundColor(IosTheme.color(.textPrimary, from: designSystem))
                    
                    Spacer()
                    
                    Toggle("", isOn: Binding(
                        get: { navigator.isDark },
                        set: { _ in navigator.toggleTheme() }
                    ))
                    .tint(IosTheme.color(.accentEnergy, from: designSystem))
                    .labelsHidden()
                }
                .padding(CGFloat(truncating: designSystem.dimen(token: .mainPadding) as NSNumber))
                .background(.ultraThinMaterial)
                .clipShape(RoundedRectangle(cornerRadius: CGFloat(truncating: designSystem.dimen(token: .cornerRadius) as NSNumber)))
            }
            .padding(CGFloat(truncating: designSystem.dimen(token: .mainPadding) as NSNumber))

            if designSystem.isDebug {
                Divider().padding(.horizontal)
                Button(action: {
                    onUserClose()
                    navigator.openDebug()
                }) {
                    HStack {
                        Text(designSystem.string(token: .openDebugSandbox))
                            .foregroundColor(IosTheme.color(.accentEnergy, from: designSystem))
                        Spacer()
                        Image(systemName: "chevron.right")
                            .font(.caption)
                            .foregroundColor(IosTheme.color(.accentEnergy, from: designSystem))
                    }
                    .padding()
                }
            }

            Spacer()

            // Footer Info
            VStack(alignment: .leading, spacing: 4) {
                Text(designSystem.string(token: .drawerFooterTitle).uppercased())
                    .font(.caption)
                    .fontWeight(.bold)
                    .foregroundColor(IosTheme.color(.textPrimary, from: designSystem))
                
                Text("\(designSystem.string(token: .drawerFooterSubtitle)) \(appConfig?.appVersion ?? "1.0.0")".uppercased())
                    .font(.caption2)
                    .foregroundColor(IosTheme.color(.accentPrimary, from: designSystem).opacity(0.8))
            }
            .padding(.horizontal, CGFloat(truncating: designSystem.dimen(token: .spacingLarge) as NSNumber))
            .padding(.bottom, 32) // Safe area bottom padding
        }
    }
}
