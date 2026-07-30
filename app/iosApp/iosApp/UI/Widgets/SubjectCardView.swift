import SwiftUI
import SharedLogic

struct SubjectCardView: View {
    let item: UiWidget.SubjectCard
    let designSystem: DesignSystem
    let onAction: (WidgetAction) -> Void
    
    private var cardWidth: CGFloat { CGFloat(truncating: designSystem.dimen(token: .subjectCardWidth) as NSNumber) }
    private var cardHeight: CGFloat { CGFloat(truncating: designSystem.dimen(token: .subjectCardHeight) as NSNumber) }
    private var iconSize: CGFloat { CGFloat(truncating: designSystem.dimen(token: .subjectCardIconSize) as NSNumber) }
    
    var body: some View {
        VStack(spacing: 20) {
            ZStack {
                Circle()
                    .fill(IosTheme.color(item.buttonColor, from: designSystem).opacity(0.1))
                    .frame(width: iconSize, height: iconSize)
                Text(item.emoji)
                    .font(.system(size: iconSize * 0.45))
            }
            
            Text(designSystem.string(token: item.titleToken))
                .font(IosTheme.font(item.titleTypography))
                .foregroundColor(IosTheme.color(item.titleColor, from: designSystem))
                .multilineTextAlignment(.center)
            
            Button(action: { onAction(item.action) }) {
                Text(designSystem.string(token: .subjectNewButton))
                    .font(.subheadline)
                    .fontWeight(.bold)
                    .padding(.vertical, 10)
                    .frame(maxWidth: .infinity)
                    .background(IosTheme.color(item.buttonColor, from: designSystem))
                    .foregroundColor(IosTheme.color(.textInverted, from: designSystem))
                    .clipShape(Capsule())
            }
        }
        .padding(20)
        .frame(width: cardWidth, height: cardHeight)
        .background(IosTheme.color(item.backgroundColor, from: designSystem).opacity(0.5))
        .clipShape(RoundedRectangle(cornerRadius: 28, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 28, style: .continuous)
                .stroke(IosTheme.color(.glassBorder, from: designSystem).opacity(0.5), lineWidth: 0.5)
        )
    }
}
