import SwiftUI
import SharedLogic

/**
 * A universal segmented tab row with a "glass" style for iOS.
 * Uses the standard native Picker with segmented style.
 */
struct GlassSegmentedTabRow<T: Hashable>: View {
    let items: [T]
    @Binding var selection: T
    let designSystem: DesignSystem
    let labelProvider: (T) -> String
    
    var body: some View {
        Picker("", selection: $selection) {
            ForEach(items, id: \.self) { item in
                Text(labelProvider(item)).tag(item)
            }
        }
        .pickerStyle(.segmented)
        .padding(designSystem.dimen(.spacingSmall))
        .padding(.horizontal, designSystem.dimen(.spacingLarge))
        .padding(.vertical, designSystem.dimen(.spacingMedium))
    }
}
