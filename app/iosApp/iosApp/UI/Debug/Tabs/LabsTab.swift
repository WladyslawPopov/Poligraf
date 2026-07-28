import SwiftUI
import SharedLogic

struct LabsTab: View {
    let designSystem: DesignSystem
    
    var body: some View {
        VStack {
            Text(designSystem.string(token: .labsEmptyMessage))
                .foregroundColor(IosTheme.color(.textSecondary, from: designSystem))
        }
        .frame(maxHeight: .infinity)
    }
}
