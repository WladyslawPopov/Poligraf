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
                    .font(.system(size: designSystem.dimen(token: .iconSizeTiny), weight: .bold))
                    .foregroundColor(designSystem.color(token: .textSecondary))
                
                Text(designSystem.string(token: .sectionRecordings).uppercased())
                    .font(.system(size: 10, weight: .bold))
                    .foregroundColor(designSystem.color(token: .textSecondary))
            }
            .padding(.horizontal, designSystem.dimen(token: .spacingLarge))
            .padding(.bottom, designSystem.dimen(token: .spacingSmall))

            VStack(alignment: .leading, spacing: 0) {
                // Selection Toolbar
                if widget.isSelectionMode {
                    selectionToolbar
                        .padding(.horizontal, designSystem.dimen(token: .spacingMedium))
                        .frame(height: designSystem.dimen(token: .headerHeight))
                        .background(designSystem.color(token: .accentPrimary).opacity(0.05))
                        .transition(.move(edge: .top).combined(with: .opacity))
                }

                VStack(spacing: 0) {
                    ForEach(Array(widget.items.enumerated()), id: \.element.id) { index, item in
                        let isSelected = widget.selectedIds.contains(item.id)
                        SubjectRowView(item: item, isSelected: isSelected, designSystem: designSystem, onAction: onAction)
                        
                        if index < widget.items.count - 1 {
                            Divider()
                                .background(designSystem.color(token: .glassBorder).opacity(0.1))
                                .padding(.horizontal, designSystem.dimen(token: .spacingMedium))
                        }
                    }
                }
                .padding(.vertical, designSystem.dimen(token: .spacingTiny))
            }
            .background(designSystem.color(token: .glassBase).opacity(0.3))
            .cornerRadius(designSystem.dimen(token: .widgetCorner))
            .overlay(
                RoundedRectangle(cornerRadius: designSystem.dimen(token: .widgetCorner))
                    .stroke(designSystem.color(token: .glassBorder).opacity(0.1), lineWidth: 1)
            )
            .padding(.horizontal, designSystem.dimen(token: .spacingMedium))
        }
    }

    private var selectionToolbar: some View {
        HStack {
            Button(action: { onAction(WidgetAction.ClearSelection()) }) {
                Image(systemName: designSystem.icon(token: .close))
                    .foregroundColor(designSystem.color(token: .textPrimary))
            }
            
            Text("\(widget.selectedIds.count) \(designSystem.string(token: .actionSelected))")
                .font(.subheadline)
                .fontWeight(.semibold)
                .foregroundColor(designSystem.color(token: .textPrimary))
            
            Spacer()
            
            Button(action: { onAction(WidgetAction.DeleteSelected()) }) {
                Image(systemName: designSystem.icon(token: .delete))
                    .foregroundColor(designSystem.color(token: .accentPrimary))
            }
            .padding(.trailing, designSystem.dimen(token: .spacingSmall))
            
            Button(action: { /* Future Menu */ }) {
                Image(systemName: designSystem.icon(token: .moreVert))
                    .foregroundColor(designSystem.color(token: .textPrimary))
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
                    .fill(designSystem.color(token: .accentPrimary))
                    .frame(width: designSystem.dimen(token: .selectionIndicatorWidth))
                    .transition(.opacity)
            }

            HStack(spacing: 16) {
                // Avatar with ring
                ZStack {
                    Circle()
                        .fill(designSystem.color(token: item.backgroundColor).opacity(0.2))
                        .frame(width: designSystem.dimen(token: .avatarSizeSmall), 
                               height: designSystem.dimen(token: .avatarSizeSmall))
                    
                    Text(item.emoji)
                        .font(.title2)
                    
                    if isSelected {
                        Circle()
                            .stroke(designSystem.color(token: .accentPrimary), lineWidth: 2)
                            .frame(width: designSystem.dimen(token: .avatarSizeSmall), 
                                   height: designSystem.dimen(token: .avatarSizeSmall))
                    }
                }
                
                VStack(alignment: .leading) {
                    Text(item.title ?? "")
                        .font(.headline)
                        .fontWeight(isSelected ? .bold : .semibold)
                        .foregroundColor(designSystem.color(token: .textPrimary))
                }
                
                Spacer()
                
                if isSelected {
                    Image(systemName: designSystem.icon(token: .check))
                        .foregroundColor(designSystem.color(token: .accentPrimary))
                        .font(.system(size: 20))
                }
            }
            .padding(.horizontal, designSystem.dimen(token: .spacingMedium))
            .frame(height: designSystem.dimen(token: .subjectRowHeight))
            .background(isSelected ? designSystem.color(token: .accentPrimary).opacity(0.12) : Color.clear)
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
