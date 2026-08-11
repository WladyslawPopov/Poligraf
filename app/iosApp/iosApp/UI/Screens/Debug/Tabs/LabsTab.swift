import SwiftUI
import SharedLogic

struct LabsTab: View {
    let designSystem: DesignSystem
    
    var body: some View {
        VStack {
            Text(designSystem.string(.labsEmptyMessage))
                .foregroundColor(designSystem.color(.textSecondary))
        }
        .frame(maxHeight: .infinity)
    }
}
