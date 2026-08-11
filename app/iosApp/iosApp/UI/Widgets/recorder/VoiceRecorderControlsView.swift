import SwiftUI
import SharedLogic

struct VoiceRecorderControlsView: View {
    let state: VoiceRecorderUiState
    let designSystem: DesignSystem
    let onAction: (VoiceRecorderAction) -> Void
    
    var body: some View {
        VStack(spacing: 32) {
            // Playback Controls
            HStack(spacing: 48) {
                Button(action: { onAction(VoiceRecorderAction.Skip(millis: -15000)) }) {
                    Image(systemName: designSystem.icon(token: state.controls.skipBackIcon))
                        .font(.system(size: designSystem.dimen(.spacingXl) + 4))
                        .foregroundColor(designSystem.color(.textPrimary))
                }
                
                Button(action: { onAction(VoiceRecorderAction.TogglePlay()) }) {
                    Image(systemName: designSystem.icon(token: state.controls.playbackIcon))
                        .font(.system(size: 56))
                        .foregroundColor(designSystem.color(.textPrimary))
                }
                
                Button(action: { onAction(VoiceRecorderAction.Skip(millis: 15000)) }) {
                    Image(systemName: designSystem.icon(token: state.controls.skipForwardIcon))
                        .font(.system(size: designSystem.dimen(.spacingXl) + 4))
                        .foregroundColor(designSystem.color(.textPrimary))
                }
            }
            
            // Record/Trim Action Bar
            HStack {
                if state.trim.isVisible {
                    Button(action: { onAction(VoiceRecorderAction.ToggleTrimMode()) }) {
                        Text(designSystem.string(.recorderTrim))
                            .font(.system(size: 17, weight: .semibold))
                            .foregroundColor(designSystem.color(.textPrimary).opacity(0.6))
                            .padding(.horizontal, designSystem.dimen(.spacingXl))
                            .padding(.vertical, designSystem.dimen(.spacingMedium))
                            .background(designSystem.color(.textPrimary).opacity(0.1))
                            .clipShape(Capsule())
                    }
                    
                    Spacer()
                    
                    Button(action: { /* Handle delete if needed */ }) {
                        Text(designSystem.string(.recorderDelete))
                            .font(.system(size: 17, weight: .semibold))
                            .foregroundColor(designSystem.color(.textPrimary).opacity(0.6))
                    }
                } else {
                    Spacer()
                    
                    // Main Record Button
                    Button(action: { onAction(VoiceRecorderAction.ToggleRecord()) }) {
                        ZStack {
                            Circle()
                                .fill(designSystem.color(state.controls.recordButtonColor))
                                .frame(width: 72, height: 72)
                            
                            Image(systemName: designSystem.icon(token: state.controls.recordIcon))
                                .font(.system(size: designSystem.dimen(.spacingXl)))
                                .foregroundColor(designSystem.color(.textInverted))
                        }
                    }
                    
                    Spacer()
                }
            }
            .padding(.horizontal, designSystem.dimen(.spacingLarge))
        }
    }
}
