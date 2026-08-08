import SwiftUI
import SharedLogic

/**
 * A universal sheet container for iOS that provides a consistent header and layout
 * matching the project's design system.
 */
struct AppSheetContainer<Content: View>: View {
    @Environment(\.horizontalSizeClass) var sizeClass
    let designSystem: DesignSystem
    let titleToken: StringToken?
    let onUserClose: (() -> Void)?
    let content: Content
    
    init(
        designSystem: DesignSystem,
        titleToken: StringToken? = nil,
        onUserClose: (() -> Void)? = nil,
        @ViewBuilder content: () -> Content
    ) {
        self.designSystem = designSystem
        self.titleToken = titleToken
        self.onUserClose = onUserClose
        self.content = content()
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Header Area
            ZStack(alignment: .bottomLeading) {
                LinearGradient(
                    colors: [designSystem.color(.accentPrimary).opacity(0.15), .clear],
                    startPoint: .top,
                    endPoint: .bottom
                )
                .frame(height: titleToken != nil ? 100 : 40)
                
                VStack(alignment: .leading, spacing: 0) {
                    HStack {
                        if let token = titleToken {
                            Text(designSystem.string(token: token))
                                .font(.system(size: 26, weight: .heavy))
                                .foregroundColor(designSystem.color(.accentPrimary))
                        }
                        
                        Spacer()
                        
                        if let closeAction = onUserClose, sizeClass != .compact {
                            Button(action: closeAction) {
                                Image(systemName: designSystem.icon(.close))
                                    .font(.system(size: 18, weight: .bold))
                                    .foregroundColor(designSystem.color(.textSecondary))
                            }
                        }
                    }
                    .padding(.horizontal, designSystem.dimen(.spacingLarge))
                    .padding(.bottom, titleToken != nil ? 16 : 0)
                    
                    if titleToken != nil {
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
            }
            
            content
            
            Spacer()
        }
        .background(.clear)
    }
}
