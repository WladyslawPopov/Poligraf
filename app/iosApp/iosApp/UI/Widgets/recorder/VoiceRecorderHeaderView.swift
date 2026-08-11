import SwiftUI
import SharedLogic

struct VoiceRecorderHeaderView: View {
    let state: VoiceRecorderUiState
    let designSystem: DesignSystem
    let onAction: (VoiceRecorderAction) -> Void
    
    var body: some View {
        ZStack {
            if state.header.isTrimming {
                HStack {
                    Button(designSystem.string(.recorderCancel)) { 
                        onAction(VoiceRecorderAction.CancelTrim()) 
                    }
                    .font(.system(size: designSystem.dimen(.textSizeTitleSmall)))
                    .foregroundColor(designSystem.color(.textPrimary))
                    
                    Spacer()
                    
                    Text(designSystem.string(.recorderTrim))
                        .font(.system(size: designSystem.dimen(.textSizeTitleSmall), weight: .bold))
                        .foregroundColor(designSystem.color(.textPrimary))
                    
                    Spacer()
                    
                    Button(designSystem.string(.recorderTrimApply)) {
                        onAction(VoiceRecorderAction.ApplyTrim(start: state.trim.startMillis, end: state.trim.endMillis))
                    }
                    .font(.system(size: designSystem.dimen(.textSizeTitleSmall), weight: .bold))
                    .foregroundColor(designSystem.color(.textPrimary).opacity(0.3)) // Matches Android's look if not changed
                }
            } else {
                HStack(spacing: designSystem.dimen(.spacingSmall)) {
                    // DISCARD (Close) Button
                    Button(action: { onAction(VoiceRecorderAction.DiscardActive()) }) {
                        ZStack {
                            Circle()
                                .fill(designSystem.color(.textPrimary).opacity(0.1))
                                .frame(width: designSystem.dimen(.recorderDragHandleWidth), height: designSystem.dimen(.recorderDragHandleWidth))
                            
                            Image(systemName: designSystem.icon(.close))
                                .font(.system(size: designSystem.dimen(.iconSizeSmall), weight: .bold))
                                .foregroundColor(designSystem.color(.textPrimary).opacity(0.6))
                        }
                    }
                    
                    // MENU Button
                    Menu {
                        Button(designSystem.string(.recorderUploadFile)) {
                            onAction(VoiceRecorderAction.UploadFromFile())
                        }
                        Button(designSystem.string(.recorderTrimMode)) {
                            onAction(VoiceRecorderAction.ToggleTrimMode())
                        }
                    } label: {
                        ZStack {
                            Circle()
                                .fill(designSystem.color(state.header.accentColor).opacity(0.15))
                                .frame(width: designSystem.dimen(.recorderDragHandleWidth), height: designSystem.dimen(.recorderDragHandleWidth))
                            
                            Image(systemName: "ellipsis")
                                .font(.system(size: designSystem.dimen(.iconSizeNav), weight: .bold))
                                .foregroundColor(designSystem.color(state.header.accentColor))
                        }
                    }
                    
                    Spacer()
                    
                    // SAVE Button
                    Button(action: { onAction(VoiceRecorderAction.SaveRecording()) }) {
                        ZStack {
                            Circle()
                                .fill(designSystem.color(state.header.accentColor).opacity(0.15))
                                .frame(width: 36, height: 36)
                            
                            Image(systemName: designSystem.icon(.check))
                                .font(.system(size: 22, weight: .bold))
                                .foregroundColor(designSystem.color(state.header.accentColor))
                        }
                    }
                }
                
                // Centered Title Info
                VStack(alignment: .center, spacing: 0) {
                    Text("\(state.header.title) \(state.header.subtitle)")
                        .font(.system(size: designSystem.dimen(.textSizeTitleMedium), weight: .bold))
                        .foregroundColor(designSystem.color(.textPrimary))
                    Text(state.header.timerLabel)
                        .font(.system(size: designSystem.dimen(.textSizeBody)))
                        .foregroundColor(designSystem.color(.textPrimary).opacity(0.5))
                }
            }
        }
        .padding(.horizontal, designSystem.dimen(.spacingMedium))
        .frame(height: 56)
    }
}

