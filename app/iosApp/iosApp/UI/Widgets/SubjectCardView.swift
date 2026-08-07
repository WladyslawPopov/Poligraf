import SwiftUI
import SharedLogic

struct SubjectCardView: View {
    let item: UiWidget.SubjectCard
    let designSystem: DesignSystem
    let onAction: (WidgetAction) -> Void
    
    private var cardWidth: CGFloat { designSystem.dimen(.subjectCardWidth) }
    private var cardHeight: CGFloat { designSystem.dimen(.subjectCardHeight) }
    private var iconSize: CGFloat { designSystem.dimen(.subjectCardIconSize) }
    
    var body: some View {
        Button(action: { onAction(item.action) }) {
            VStack(spacing: 24) {
                ZStack {
                    Circle()
                        .fill(designSystem.color(item.buttonColor).opacity(0.1))
                        .frame(width: iconSize, height: iconSize)
                    Text(item.emoji)
                        .font(.system(size: iconSize * 0.45))
                }
                
                Text(item.title ?? designSystem.string(token: item.titleToken))
                    .font(designSystem.font(item.titleTypography))
                    .foregroundColor(designSystem.color(item.titleColor))
                    .multilineTextAlignment(.center)
                    .lineLimit(2)
            }
            .padding(20)
            .frame(width: cardWidth, height: cardHeight)
            .background(designSystem.color(item.backgroundColor).opacity(0.5))
            .clipShape(RoundedRectangle(cornerRadius: designSystem.dimen(.widgetCorner), style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: designSystem.dimen(.widgetCorner), style: .continuous)
                    .stroke(designSystem.color(.glassBorder).opacity(0.5), lineWidth: 0.5)
            )
        }
        .buttonStyle(.plain)
    }
}
