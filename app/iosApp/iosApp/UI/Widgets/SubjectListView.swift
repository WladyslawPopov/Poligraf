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
                Image(systemName: designSystem.icon(.history))
                    .font(.system(size: designSystem.dimen(.iconSizeTiny), weight: .bold))
                    .foregroundColor(designSystem.color(.textSecondary))
                
                Text(designSystem.string(token: .sectionRecordings).uppercased())
                    .font(.system(size: 10, weight: .bold))
                    .foregroundColor(designSystem.color(.textSecondary))
            }
            .padding(.horizontal, designSystem.dimen(.spacingLarge))
            .padding(.bottom, designSystem.dimen(.spacingSmall))

            VStack(alignment: .leading, spacing: 0) {
                // Selection Toolbar
                if widget.isSelectionMode {
                    selectionToolbar
                        .padding(.horizontal, designSystem.dimen(.spacingMedium))
                        .frame(height: designSystem.dimen(.headerHeight))
                        .background(designSystem.color(.accentPrimary).opacity(0.05))
                        .transition(.move(edge: .top).combined(with: .opacity))
                }

                VStack(spacing: 0) {
                    ForEach(Array(widget.items.enumerated()), id: \.element.id) { index, item in
                        let isSelected = widget.selectedIds.contains(item.id)
                        SubjectRowView(item: item, isSelected: isSelected, designSystem: designSystem, onAction: onAction)
                        
                        if index < widget.items.count - 1 {
                            Divider()
                                .background(designSystem.color(.glassBorder).opacity(0.1))
                                .padding(.horizontal, designSystem.dimen(.spacingMedium))
                        }
                    }
                }
                .padding(.vertical, designSystem.dimen(.spacingTiny))
            }
            .background(designSystem.color(.glassBase).opacity(0.3))
            .cornerRadius(designSystem.dimen(.widgetCorner))
            .overlay(
                RoundedRectangle(cornerRadius: designSystem.dimen(.widgetCorner))
                    .stroke(designSystem.color(.glassBorder).opacity(0.1), lineWidth: 1)
            )
            .padding(.horizontal, designSystem.dimen(.spacingMedium))
        }
    }

    private var selectionToolbar: some View {
        HStack {
            Button(action: { onAction(WidgetAction.ClearSelection()) }) {
                Image(systemName: designSystem.icon(.close))
                    .foregroundColor(designSystem.color(.textPrimary))
            }
            
            Text("\(widget.selectedIds.count) selected")
                .font(.subheadline)
                .fontWeight(.semibold)
                .foregroundColor(designSystem.color(.textPrimary))
            
            Spacer()
            
            Button(action: { onAction(WidgetAction.DeleteSelected()) }) {
                Image(systemName: designSystem.icon(.delete))
                    .foregroundColor(designSystem.color(.accentPrimary))
            }
            .padding(.trailing, designSystem.dimen(.spacingSmall))
            
            Button(action: { /* Future Menu */ }) {
                Image(systemName: designSystem.icon(.moreVert))
                    .foregroundColor(designSystem.color(.textPrimary))
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
                    .fill(designSystem.color(.accentPrimary))
                    .frame(width: designSystem.dimen(.selectionIndicatorWidth))
                    .transition(.opacity)
            }

            HStack(spacing: 16) {
                // Avatar with ring
                ZStack {
                    Circle()
                        .fill(designSystem.color(item.backgroundColor).opacity(0.2))
                        .frame(width: designSystem.dimen(.avatarSizeSmall), 
                               height: designSystem.dimen(.avatarSizeSmall))
                    
                    Text(item.emoji)
                        .font(.title2)
                    
                    if isSelected {
                        Circle()
                            .stroke(designSystem.color(.accentPrimary), lineWidth: 2)
                            .frame(width: designSystem.dimen(.avatarSizeSmall), 
                                   height: designSystem.dimen(.avatarSizeSmall))
                    }
                }
                
                VStack(alignment: .leading) {
                    Text(item.title ?? "")
                        .font(.headline)
                        .fontWeight(isSelected ? .bold : .semibold)
                        .foregroundColor(designSystem.color(.textPrimary))
                }
                
                Spacer()
                
                if isSelected {
                    Image(systemName: designSystem.icon(.check))
                        .foregroundColor(designSystem.color(.accentPrimary))
                        .font(.system(size: 20))
                }
            }
            .padding(.horizontal, designSystem.dimen(.spacingMedium))
            .frame(height: designSystem.dimen(.subjectRowHeight))
            .background(isSelected ? designSystem.color(.accentPrimary).opacity(0.12) : Color.clear)
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
