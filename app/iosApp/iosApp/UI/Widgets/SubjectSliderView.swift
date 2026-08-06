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
                    .foregroundColor(IosTheme.color(.textSecondary, from: designSystem))
            }
            .padding(.horizontal, CGFloat(truncating: designSystem.dimen(token: .spacingLarge) as NSNumber))
            .padding(.bottom, CGFloat(truncating: designSystem.dimen(token: .spacingSmall) as NSNumber))

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
                .padding(.horizontal, CGFloat(truncating: designSystem.dimen(token: .spacingMedium) as NSNumber))
            }
        }
        .padding(.vertical, CGFloat(truncating: designSystem.dimen(token: .spacingSmall) as NSNumber))
    }
}

struct SubjectStoryView: View {
    let item: UiWidget.SubjectCard
    let designSystem: DesignSystem
    let onAction: (WidgetAction) -> Void
    
    var body: some View {
        Button(action: { onAction(item.action) }) {
            VStack(spacing: CGFloat(truncating: designSystem.dimen(token: .spacingTiny) as NSNumber)) {
                Text(item.emoji)
                    .font(.system(size: 40))
                
                Text(item.title ?? designSystem.string(token: item.titleToken))
                    .font(.caption2)
                    .fontWeight(.medium)
                    .multilineTextAlignment(.center)
                    .lineLimit(2)
                    .foregroundColor(IosTheme.color(item.titleColor, from: designSystem))
            }
            .frame(width: CGFloat(truncating: designSystem.dimen(token: .subjectStoryWidth) as NSNumber),
                   height: CGFloat(truncating: designSystem.dimen(token: .subjectStoryHeight) as NSNumber))
            .background(IosTheme.color(item.backgroundColor, from: designSystem).opacity(0.4))
            .cornerRadius(CGFloat(truncating: designSystem.dimen(token: .cornerRadius) as NSNumber))
            .overlay(
                RoundedRectangle(cornerRadius: CGFloat(truncating: designSystem.dimen(token: .cornerRadius) as NSNumber))
                    .stroke(IosTheme.color(.glassBorder, from: designSystem).opacity(0.15), lineWidth: 1)
            )
        }
    }
}
