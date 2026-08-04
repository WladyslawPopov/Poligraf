import SwiftUI
import SharedLogic

struct SubjectListView: View {
    let widget: UiWidget.SubjectList
    let designSystem: DesignSystem
    let onAction: (WidgetAction) -> Void
    
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(designSystem.string(token: .sectionSubjects))
                .font(.caption2)
                .foregroundColor(IosTheme.color(.textSecondary, from: designSystem))
                .padding(.horizontal, CGFloat(truncating: designSystem.dimen(token: .spacingMedium) as NSNumber))
                .padding(.top, 16)
                .padding(.bottom, 8)
            
            ForEach(widget.items, id: \.id) { item in
                let isSelected = widget.selectedIds.contains(item.id)
                VStack(spacing: 0) {
                    SubjectRowView(item: item, isSelected: isSelected, designSystem: designSystem, onAction: onAction)
                    
                    if item.id != widget.items.last?.id {
                        Divider()
                            .overlay(IosTheme.color(.glassBorder, from: designSystem).opacity(0.4))
                            .padding(.leading, CGFloat(truncating: designSystem.dimen(token: .avatarSizeSmall) as NSNumber) + 32)
                            .padding(.trailing, 16)
                    }
                }
            }
        }
    }
}

struct SubjectRowView: View {
    let item: UiWidget.SubjectCard
    let isSelected: Bool
    let designSystem: DesignSystem
    let onAction: (WidgetAction) -> Void
    
    var body: some View {
        HStack(spacing: 16) {
            ZStack {
                Circle()
                    .fill(IosTheme.color(item.buttonColor, from: designSystem).opacity(0.1))
                Text(item.emoji)
                    .font(.title2)
            }
            .frame(width: CGFloat(truncating: designSystem.dimen(token: .avatarSizeSmall) as NSNumber),
                   height: CGFloat(truncating: designSystem.dimen(token: .avatarSizeSmall) as NSNumber))
            
            VStack(alignment: .leading) {
                Text(item.title ?? designSystem.string(token: item.titleToken))
                    .font(.headline)
                    .foregroundColor(IosTheme.color(item.titleColor, from: designSystem))
            }
            
            Spacer()
            
            if isSelected {
                Image(systemName: designSystem.icon(token: .check))
                    .foregroundColor(IosTheme.color(.accentPrimary, from: designSystem))
            } else {
                Image(systemName: designSystem.icon(token: .chevronRight))
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(IosTheme.color(.textSecondary, from: designSystem).opacity(0.5))
            }
        }
        .padding(.horizontal, CGFloat(truncating: designSystem.dimen(token: .spacingMedium) as NSNumber))
        .frame(height: CGFloat(truncating: designSystem.dimen(token: .subjectRowHeight) as NSNumber))
        .background(isSelected ? IosTheme.color(.accentPrimary, from: designSystem).opacity(0.1) : Color.clear)
        .contentShape(Rectangle())
        .onTapGesture {
            onAction(item.action)
        }
        .onLongPressGesture {
            onAction(WidgetAction.ToggleSelection(id: item.id))
        }
    }
}
