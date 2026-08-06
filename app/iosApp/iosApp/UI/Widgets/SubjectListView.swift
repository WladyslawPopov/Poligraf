import SwiftUI
import SharedLogic

struct SubjectListView: View {
    let widget: UiWidget.SubjectList
    let designSystem: DesignSystem
    let onAction: (WidgetAction) -> Void
    
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Header moved OUTSIDE the container
            HStack(spacing: 6) {
                Image(systemName: designSystem.icon(token: .history))
                    .font(.system(size: CGFloat(truncating: designSystem.dimen(token: .iconSizeTiny) as NSNumber), weight: .bold))
                    .foregroundColor(IosTheme.color(.textSecondary, from: designSystem))
                
                Text(designSystem.string(token: .sectionSubjects).uppercased())
                    .font(.system(size: 10, weight: .bold))
                    .foregroundColor(IosTheme.color(.textSecondary, from: designSystem))
            }
            .padding(.horizontal, CGFloat(truncating: designSystem.dimen(token: .spacingLarge) as NSNumber))
            .padding(.bottom, CGFloat(truncating: designSystem.dimen(token: .spacingSmall) as NSNumber))

            VStack(alignment: .leading, spacing: 0) {
                // Selection Toolbar
                if widget.isSelectionMode {
                    selectionToolbar
                        .padding(.horizontal, CGFloat(truncating: designSystem.dimen(token: .spacingMedium) as NSNumber))
                        .frame(height: CGFloat(truncating: designSystem.dimen(token: .headerHeight) as NSNumber))
                        .background(IosTheme.color(.accentPrimary, from: designSystem).opacity(0.05))
                        .transition(.move(edge: .top).combined(with: .opacity))
                }

                VStack(spacing: 0) {
                    ForEach(Array(widget.items.enumerated()), id: \.element.id) { index, item in
                        let isSelected = widget.selectedIds.contains(item.id)
                        SubjectRowView(item: item, isSelected: isSelected, designSystem: designSystem, onAction: onAction)
                        
                        if index < widget.items.count - 1 {
                            Divider()
                                .background(IosTheme.color(.glassBorder, from: designSystem).opacity(0.1))
                                .padding(.horizontal, CGFloat(truncating: designSystem.dimen(token: .spacingMedium) as NSNumber))
                        }
                    }
                }
                .padding(.vertical, CGFloat(truncating: designSystem.dimen(token: .spacingTiny) as NSNumber))
            }
            .background(IosTheme.color(.glassBase, from: designSystem).opacity(0.3))
            .cornerRadius(CGFloat(truncating: designSystem.dimen(token: .widgetCorner) as NSNumber))
            .overlay(
                RoundedRectangle(cornerRadius: CGFloat(truncating: designSystem.dimen(token: .widgetCorner) as NSNumber))
                    .stroke(IosTheme.color(.glassBorder, from: designSystem).opacity(0.1), lineWidth: 1)
            )
            .padding(.horizontal, CGFloat(truncating: designSystem.dimen(token: .spacingMedium) as NSNumber))
        }
    }

    private var selectionToolbar: some View {
        HStack {
            Button(action: { onAction(WidgetAction.ClearSelection()) }) {
                Image(systemName: designSystem.icon(token: .close))
                    .foregroundColor(IosTheme.color(.textPrimary, from: designSystem))
            }
            
            Text("\(widget.selectedIds.count) selected")
                .font(.subheadline)
                .fontWeight(.semibold)
                .foregroundColor(IosTheme.color(.textPrimary, from: designSystem))
            
            Spacer()
            
            Button(action: { onAction(WidgetAction.DeleteSelected()) }) {
                Image(systemName: designSystem.icon(token: .delete))
                    .foregroundColor(IosTheme.color(.accentPrimary, from: designSystem))
            }
            .padding(.trailing, CGFloat(truncating: designSystem.dimen(token: .spacingSmall) as NSNumber))
            
            Button(action: { /* Future Menu */ }) {
                Image(systemName: designSystem.icon(token: .moreVert))
                    .foregroundColor(IosTheme.color(.textPrimary, from: designSystem))
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
        ZStack(alignment: .leading) {
            // Neon Indicator
            if isSelected {
                Rectangle()
                    .fill(IosTheme.color(.accentPrimary, from: designSystem))
                    .frame(width: CGFloat(truncating: designSystem.dimen(token: .selectionIndicatorWidth) as NSNumber))
                    .transition(.opacity)
            }

            HStack(spacing: 16) {
                // Avatar with ring
                ZStack {
                    Circle()
                        .fill(IosTheme.color(item.backgroundColor, from: designSystem).opacity(0.2))
                        .frame(width: CGFloat(truncating: designSystem.dimen(token: .avatarSizeSmall) as NSNumber), 
                               height: CGFloat(truncating: designSystem.dimen(token: .avatarSizeSmall) as NSNumber))
                    
                    Text(item.emoji)
                        .font(.title2)
                    
                    if isSelected {
                        Circle()
                            .stroke(IosTheme.color(.accentPrimary, from: designSystem), lineWidth: 2)
                            .frame(width: CGFloat(truncating: designSystem.dimen(token: .avatarSizeSmall) as NSNumber), 
                                   height: CGFloat(truncating: designSystem.dimen(token: .avatarSizeSmall) as NSNumber))
                    }
                }
                
                VStack(alignment: .leading) {
                    Text(item.title ?? "")
                        .font(.headline)
                        .fontWeight(isSelected ? .bold : .semibold)
                        .foregroundColor(IosTheme.color(.textPrimary, from: designSystem))
                }
                
                Spacer()
                
                if isSelected {
                    Image(systemName: designSystem.icon(token: .check))
                        .foregroundColor(IosTheme.color(.accentPrimary, from: designSystem))
                        .font(.system(size: 20))
                }
            }
            .padding(.horizontal, CGFloat(truncating: designSystem.dimen(token: .spacingMedium) as NSNumber))
            .frame(height: CGFloat(truncating: designSystem.dimen(token: .subjectRowHeight) as NSNumber))
            .background(isSelected ? IosTheme.color(.accentPrimary, from: designSystem).opacity(0.12) : Color.clear)
        }
        .contentShape(Rectangle())
        .onTapGesture {
            onAction(item.action)
        }
        .onLongPressGesture {
            onAction(WidgetAction.ToggleSelection(id: item.id))
        }
    }
}
