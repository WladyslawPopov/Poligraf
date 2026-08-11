import SwiftUI
import SharedLogic

struct RecordingsHistoryView: View {
    @ObservedObject var navigator: IosNavigator
    let component: RecordingsHistoryComponent
    let designSystem: DesignSystem
    
    @ObservedObject var state: SKIEStateObserver<RecordingState>
    @ObservedObject var recorderUiState: SKIEStateObserver<VoiceRecorderUiState>
    @ObservedObject var activeRecorder: SKIEOptionalStateObserver<VoiceRecorder>
    @ObservedObject var historicalRecordings: SKIEStateObserver<[VoiceRecorder]>
    
    @State private var isSelectionMode = false
    @State private var selectedIds: Set<String> = []
    @State private var isPulseActive = false
    @State private var contentVisible: Bool = false

    init(navigator: IosNavigator, component: RecordingsHistoryComponent, designSystem: DesignSystem) {
        self.navigator = navigator
        self.component = component
        self.designSystem = designSystem
        self._state = ObservedObject(wrappedValue: SKIEStateObserver(component.viewModel.state))
        self._recorderUiState = ObservedObject(wrappedValue: SKIEStateObserver(component.viewModel.recorderUiState))
        self._activeRecorder = ObservedObject(wrappedValue: SKIEOptionalStateObserver(component.viewModel.activeRecorder))
        self._historicalRecordings = ObservedObject(wrappedValue: SKIEStateObserver(component.viewModel.historicalRecordings))
    }

    var body: some View {
        AppScaffold(
            viewModel: component.viewModel,
            state: state.value,
            designSystem: designSystem,
            onRetry: { },
            onRefresh: { }
        ) {
            mainContent
        }
        .onAppear {
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
                withAnimation(.easeOut(duration: 0.6)) {
                    contentVisible = true
                }
            }
        }
        .toolbar {
            navigationToolbar
        }
    }

    private var mainContent: some View {
        ZStack(alignment: .top) {
            ScrollView {
                VStack(spacing: 0) {
                    recordingList
                }
                .padding(.top, 100) // Padding for transparent toolbar
            }
            .ignoresSafeArea(edges: .top)
            .refreshable {
                component.loadContent()
            }
            .scrollContentBackground(.hidden)
            
            // Selection Actions Bar
            if isSelectionMode && !selectedIds.isEmpty {
                VStack {
                    Spacer()
                    HStack {
                        Text("Selected: \(selectedIds.count)")
                            .font(.headline)
                            .foregroundColor(designSystem.color(token: .textPrimary))
                        
                        Spacer()
                        
                        Button(action: {
                            selectedIds.forEach { id in
                                component.handleVoiceAction(action: VoiceRecorderAction.DeleteRecording(id: id))
                            }
                            selectedIds.removeAll()
                            isSelectionMode = false
                        }) {
                            Image(systemName: designSystem.icon(.delete))
                                .font(.system(size: 20, weight: .bold))
                                .foregroundColor(.white)
                                .padding(12)
                                .background(designSystem.color(token: .recorderPrimary))
                                .clipShape(Circle())
                        }
                    }
                    .padding(.horizontal, 24)
                    .padding(.vertical, 16)
                    .background(
                        RoundedRectangle(cornerRadius: 24)
                            .fill(designSystem.color(token: .recorderSurface).opacity(0.9))
                            .shadow(radius: 10)
                    )
                    .padding(.horizontal, 20)
                    .padding(.bottom, 40)
                }
                .transition(.move(edge: .bottom).combined(with: .opacity))
                .zIndex(10)
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .safeAreaInset(edge: .bottom) {
            if activeRecorder.value == nil && !isSelectionMode {
                micButton
            }
        }
        .sheet(isPresented: Binding(
            get: { activeRecorder.value != nil },
            set: { isPresented in
                if !isPresented && activeRecorder.value != nil {
                    if !(recorderUiState.value.waveform.isRecording) {
                        component.handleVoiceAction(action: VoiceRecorderAction.DiscardActive())
                    }
                }
            }
        )) {
            recorderSheet
        }
    }

    private var recordingList: some View {
        VStack(alignment: .leading, spacing: designSystem.dimen(.widgetSpacing)) {
            if contentVisible {
                // List Items
                VStack(spacing: 8) {
                    if historicalRecordings.value.isEmpty {
                        EmptyHistoryView(designSystem: designSystem)
                    } else {
                        ForEach(historicalRecordings.value, id: \.id) { recorder in
                            RecordingRow(
                                recorder: recorder,
                                isSelected: selectedIds.contains(recorder.id),
                                isSelectionMode: isSelectionMode,
                                designSystem: designSystem,
                                onClick: {
                                    if isSelectionMode {
                                        if selectedIds.contains(recorder.id) {
                                            selectedIds.remove(recorder.id)
                                        } else {
                                            selectedIds.insert(recorder.id)
                                        }
                                    } else {
                                        component.onRecordingClicked(recorder: recorder)
                                    }
                                }
                            )
                        }
                    }
                }
                .padding(.top, 16)
                .padding(.horizontal, designSystem.dimen(token: .spacingMedium))
            }
        }
        .padding(.bottom, 120)
    }

    private var recorderSheet: some View {
        VoiceRecorderView(
            state: recorderUiState.value,
            designSystem: designSystem,
            onAction: { action in
                component.handleVoiceAction(action: action)
            },
            onSave: { url, duration, amplitudes in
                component.viewModel.onRecordingSaved(
                    id: recorderUiState.value.id,
                    path: url.path,
                    duration: duration,
                    amplitudes: amplitudes.map { KotlinFloat(value: Float($0)) }
                )
            }
        )
        .environment(\.colorScheme, navigator.isDark ? .dark : .light)
        .presentationDetents([.large])
        .presentationDragIndicator(.visible)
        .presentationBackground(designSystem.color(.surface).opacity(0.8))
    }

    private var micButton: some View {
        Button(action: { component.onMicClicked() }) {
            ZStack {
                Circle()
                    .stroke(designSystem.color(token: .textPrimary).opacity(0.1), lineWidth: 4)
                    .frame(width: designSystem.dimen(token: .subjectCardIconSize) + 6, 
                           height: designSystem.dimen(token: .subjectCardIconSize) + 6)
                
                Circle()
                    .fill(designSystem.color(token: .recorderWaveform))
                    .frame(width: designSystem.dimen(token: .subjectCardIconSize) - 10, 
                           height: designSystem.dimen(token: .subjectCardIconSize) - 10)
                    .scaleEffect(isPulseActive ? 1.1 : 1.0)
                
                Image(systemName: designSystem.icon(.mic))
                    .font(.system(size: designSystem.dimen(token: .spacingXl)))
                    .foregroundColor(designSystem.color(token: .textInverted))
            }
        }
        .padding(.bottom, designSystem.dimen(token: .spacingXl) * 2)
        .onAppear {
            withAnimation(.easeInOut(duration: 1.0).repeatForever(autoreverses: true)) {
                isPulseActive = true
            }
        }
    }

    @ToolbarContentBuilder
    private var navigationToolbar: some ToolbarContent {
        ToolbarItem(placement: .principal) {
            Text(designSystem.string(token: .recorderHistoryTitle))
                .font(.headline)
                .foregroundColor(designSystem.color(.textPrimary))
        }

        ToolbarItem(placement: .navigationBarTrailing) {
            Button(action: {
                withAnimation(.spring()) {
                    isSelectionMode.toggle()
                    if !isSelectionMode {
                        selectedIds.removeAll()
                    }
                }
            }) {
                Text(designSystem.string(token: isSelectionMode ? .recorderCancel : .recorderHistorySelect))
                    .font(.system(size: 14, weight: .bold))
                    .foregroundColor(designSystem.color(token: .accentPrimary))
                    .padding(.horizontal, 12)
                    .padding(.vertical, 6)
                    .background(designSystem.color(token: .glassBase).opacity(0.25))
                    .cornerRadius(8)
            }
        }
    }
}

struct RecordingRow: View {
    let recorder: VoiceRecorder
    let isSelected: Bool
    let isSelectionMode: Bool
    let designSystem: DesignSystem
    let onClick: () -> Void
    
    var body: some View {
        Button(action: onClick) {
            HStack(spacing: 16) {
                if isSelectionMode {
                    ZStack {
                        Circle()
                            .fill(isSelected ? designSystem.color(token: .recorderPrimary) : designSystem.color(token: .textPrimary).opacity(0.1))
                            .frame(width: 20, height: 20)
                        
                        if isSelected {
                            Image(systemName: "checkmark")
                                .font(.system(size: 10, weight: .bold))
                                .foregroundColor(.white)
                        }
                    }
                    .overlay(Circle().stroke(designSystem.color(token: .textPrimary).opacity(0.2), lineWidth: 1))
                }

                VStack(alignment: .leading, spacing: 2) {
                    Text(recorder.title)
                        .font(.system(size: 16, weight: .black))
                        .foregroundColor(designSystem.color(token: .textPrimary))
                    
                    Text(designSystem.string(token: .recorderToday))
                        .font(.system(size: 12, weight: .medium))
                        .foregroundColor(designSystem.color(token: .textPrimary).opacity(0.4))
                }
                
                Spacer()
                
                Text(formatDuration(millis: recorder.durationMillis))
                    .font(.system(size: 13, weight: .bold))
                    .foregroundColor(designSystem.color(token: .textPrimary).opacity(0.4))
            }
            .padding(.horizontal, designSystem.dimen(token: .spacingLarge))
            .padding(.vertical, designSystem.dimen(token: .spacingMedium))
            .background(
                isSelected ? 
                designSystem.color(token: .recorderPrimary).opacity(0.1) : 
                designSystem.color(token: .glassBase).opacity(0.15)
            )
            .cornerRadius(12)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(isSelected ? designSystem.color(token: .recorderPrimary).opacity(0.5) : Color.clear, lineWidth: 2)
            )
            .scaleEffect(isSelected ? 0.98 : 1.0)
        }
        .buttonStyle(PlainButtonStyle())
    }
    
    private func formatDuration(millis: Int64) -> String {
        let seconds = (millis / 1000) % 60
        let minutes = (millis / (1000 * 60)) % 60
        return String(format: "%02d:%02d", minutes, seconds)
    }
}

struct EmptyHistoryView: View {
    let designSystem: DesignSystem
    
    var body: some View {
        VStack(spacing: 16) {
            Spacer().frame(height: 40)
            Text(designSystem.string(token: .recorderHistoryEmpty))
                .font(.headline)
                .foregroundColor(designSystem.color(token: .textPrimary).opacity(0.7))
                .multilineTextAlignment(.center)
                .padding(.horizontal, 40)
            Spacer().frame(height: 40)
        }
    }
}
