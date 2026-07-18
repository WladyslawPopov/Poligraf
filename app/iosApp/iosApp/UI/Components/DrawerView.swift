import SwiftUI
import SharedLogic

struct DrawerView: View {
    @Environment(\.horizontalSizeClass) var sizeClass
    @ObservedObject var root: RootComponentWrapper
    let designSystem: DesignSystem
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
                
                // Show explicit close button only on iPad (Sidebar)
                // On iPhone, native sheet has a drag indicator and swipe-to-close
                if sizeClass != .compact {
                    Button(action: onUserClose) {
                        Image(systemName: designSystem.icon(token: .close))
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(IosTheme.color(.textSecondary, from: designSystem))
                    }
                }
            }
            .padding(.top, sizeClass == .compact ? CGFloat(designSystem.dimen(token: .paddingError)) : CGFloat(designSystem.dimen(token: .headerHeight)))
            .padding(.horizontal, CGFloat(designSystem.dimen(token: .mainPadding)))
            
            VStack(alignment: .leading, spacing: CGFloat(designSystem.dimen(token: .spacingLarge))) {
                HStack {
                    Text(designSystem.string(token: .drawerDarkMode))
                        .foregroundColor(IosTheme.color(.textPrimary, from: designSystem))
                    
                    Spacer()
                    
                    Toggle("", isOn: Binding(
                        get: { root.isDark },
                        set: { _ in root.toggleTheme() }
                    ))
                    .tint(IosTheme.color(.accentEnergy, from: designSystem))
                    .labelsHidden()
                }
                .padding(CGFloat(designSystem.dimen(token: .mainPadding)))
                .background(.ultraThinMaterial)
                .clipShape(RoundedRectangle(cornerRadius: CGFloat(designSystem.dimen(token: .cornerRadius))))
            }
            .padding(CGFloat(designSystem.dimen(token: .mainPadding)))


            Spacer()
        }
    }
}
