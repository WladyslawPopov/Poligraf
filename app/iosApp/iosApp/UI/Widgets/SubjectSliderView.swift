import SwiftUI
import SharedLogic

struct SubjectSliderView: View {
    let widget: UiWidget.SubjectSlider
    let designSystem: DesignSystem
    let onAction: (WidgetAction) -> Void
    
    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: CGFloat(truncating: widget.itemSpacing as NSNumber)) {
                ForEach(widget.items, id: \.id) { item in
                    SubjectCardView(item: item, designSystem: designSystem, onAction: onAction)
                }
            }
            .padding(.horizontal, CGFloat(truncating: designSystem.dimen(token: .spacingMedium) as NSNumber))
            .scrollTargetLayout()
        }
        .scrollTargetBehavior(.viewAligned)
    }
}
