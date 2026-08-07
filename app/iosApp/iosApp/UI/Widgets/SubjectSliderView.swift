import SwiftUI
import SharedLogic

struct SubjectSliderView: View {
    let widget: UiWidget.SubjectSlider
    let designSystem: DesignSystem
    let onAction: (WidgetAction) -> Void
    
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Header for Slider (Templates)
            HStack(spacing: 6) {
                Text(designSystem.string(token: .sectionTemplates).uppercased())
                    .font(.system(size: 10, weight: .bold))
                    .foregroundColor(designSystem.color(.textSecondary))
            }
            .padding(.horizontal, designSystem.dimen(.spacingLarge))
            .padding(.bottom, designSystem.dimen(.spacingSmall))

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: CGFloat(truncating: widget.itemSpacing as NSNumber)) {
                    ForEach(widget.items, id: \.id) { item in
                        if widget.displayMode == .rectStory {
                            SubjectStoryView(item: item, designSystem: designSystem, onAction: onAction)
                        } else {
                            SubjectCardView(item: item, designSystem: designSystem, onAction: onAction)
                        }
                    }
                }
                .padding(.horizontal, designSystem.dimen(.spacingMedium))
            }
        }
        .padding(.vertical, designSystem.dimen(.spacingSmall))
    }
}

struct SubjectStoryView: View {
    let item: UiWidget.SubjectCard
    let designSystem: DesignSystem
    let onAction: (WidgetAction) -> Void
    
    var body: some View {
        Button(action: { onAction(item.action) }) {
            VStack(spacing: 12) {
                Text(item.emoji)
                    .font(.system(size: 40))
                
                Text(item.title ?? designSystem.string(token: item.titleToken))
                    .font(.system(size: 10))
                    .fontWeight(.medium)
                    .multilineTextAlignment(.center)
                    .lineLimit(2)
                    .foregroundColor(designSystem.color(item.titleColor))
            }
            .frame(width: designSystem.dimen(.subjectStoryWidth),
                   height: designSystem.dimen(.subjectStoryHeight))
            .background(designSystem.color(item.backgroundColor).opacity(0.4))
            .cornerRadius(designSystem.dimen(.cornerRadius))
            .overlay(
                RoundedRectangle(cornerRadius: designSystem.dimen(.cornerRadius))
                    .stroke(designSystem.color(.glassBorder).opacity(0.15), lineWidth: 1)
            )
        }
    }
}
