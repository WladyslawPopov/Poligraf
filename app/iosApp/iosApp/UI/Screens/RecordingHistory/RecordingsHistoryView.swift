import SwiftUI
import SharedLogic

struct RecordingsHistoryView: View {
    @ObservedObject var navigator: IosNavigator
    let component: RecordingsHistoryComponent
    let designSystem: DesignSystem
    
    @ObservedObject var state: SKIEStateObserver<RecordingState>
    @State private var isSelectionMode = false
    @State private var selectedIds: Set<String> = []

    init(navigator: IosNavigator, component: RecordingsHistoryComponent, designSystem: DesignSystem) {
        self.navigator = navigator
        self.component = component
        self.designSystem = designSystem
        self._state = ObservedObject(wrappedValue: SKIEStateObserver(component.viewModel.state))
    }

    var body: some View {
        ZStack(alignment: .bottom) {
            AppScaffold(
                viewModel: component.viewModel,
                state: state.value,
                designSystem: designSystem,
                onRetry: { },
                onRefresh: { }
            ) {
                VStack(spacing: 0) {
                    List {
                        ForEach(state.value.widgets.compactMap { $0 as? UiWidget.VoiceRecorder }, id: \.id) { recorder in
                            RecordingRow(
                                recorder: recorder,
                                isSelectionMode: isSelectionMode,
                                isSelected: selectedIds.contains(recorder.id),
                                designSystem: designSystem
                            ) {
                                if isSelectionMode {
                                    if selectedIds.contains(recorder.id) {
                                        selectedIds.remove(recorder.id)
                                    } else {
                                        selectedIds.insert(recorder.id)
                                    }
                                } else {
                                    // Expand recorder (already in state but let's trigger)
                                    component.toggleExpand()
                                }
                            }
                            .listRowBackground(Color.white.opacity(0.05))
                        }
                    }
                    .scrollContentBackground(.hidden)
                    .background(designSystem.color(.background))
                }
            }
            .navigationTitle("All Recordings")
            .navigationBarTitleDisplayMode(.large)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(isSelectionMode ? "Done" : "Edit") {
                        isSelectionMode.toggle()
                    }
                    .foregroundColor(designSystem.color(.accentPrimary))
                }
                
                if isSelectionMode {
                    ToolbarItem(placement: .bottomBar) {
                        Button(role: .destructive) {
                            // Delete logic
                        } label: {
                            Text("Delete \(selectedIds.count)")
                        }
                        .disabled(selectedIds.isEmpty)
                    }
                }
            }

            // Big Red Button for new recording
            if state.value.activeRecorder == nil && !isSelectionMode {
                Button(action: { component.onMicClicked() }) {
                    Circle()
                        .fill(designSystem.color(.error))
                        .frame(width: 80, height: 80)
                        .overlay(Circle().stroke(Color.white.opacity(0.2), lineWidth: 4))
                        .shadow(radius: 10)
                }
                .padding(.bottom, 40)
            }

            // Bottom Recorder Sheet
            if let recorder = state.value.activeRecorder {
                VoiceRecorderView(
                    widget: recorder,
                    designSystem: designSystem,
                    onToggle: { component.toggleRecording() },
                    onStop: { component.stopRecording() },
                    onPlay: { component.onPlayClicked() },
                    onPause: { component.onPausePlaybackClicked() },
                    onSeek: { component.onSeek(position: $0) },
                    onTrimUpdate: { component.onTrimUpdate(start: $0, end: $1) },
                    onTrimCancel: { component.onTrimCancel() },
                    onTrimApply: { component.onTrim(start: $0, end: $1) },
                    onReplace: { component.onReplaceClicked() },
                    onSave: { component.onSaveClicked() },
                    onResume: { component.onResumeRecording() },
                    onToggleTrim: { component.toggleTrimMode() },
                    onSkip: { component.onSkip(millis: $0) }
                )
                .transition(.move(edge: .bottom))
                .animation(.spring(), value: state.value.activeRecorder != nil)
                .frame(height: recorder.isExpanded ? UIScreen.main.bounds.height * 0.9 : 140)
            }
        }
        .ignoresSafeArea(.all, edges: .bottom)
    }
}

struct RecordingRow: View {
    let recorder: UiWidget.VoiceRecorder
    let isSelectionMode: Bool
    let isSelected: Bool
    let designSystem: DesignSystem
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack {
                if isSelectionMode {
                    Image(systemName: isSelected ? "checkmark.circle.fill" : "circle")
                        .foregroundColor(isSelected ? designSystem.color(.accentPrimary) : .gray)
                }
                
                VStack(alignment: .leading) {
                    Text(recorder.title)
                        .font(.headline)
                        .foregroundColor(.white)
                    Text("15 May 2026")
                        .font(.subheadline)
                        .foregroundColor(.gray)
                }
                Spacer()
                Text(formatDuration(millis: recorder.durationMillis))
                    .foregroundColor(.gray)
            }
            .padding(.vertical, 8)
        }
    }
    
    private func formatDuration(millis: Int64) -> String {
        let seconds = (millis / 1000) % 60
        let minutes = (millis / (1000 * 60)) % 60
        return String(format: "%02d:%02d", minutes, seconds)
    }
}
