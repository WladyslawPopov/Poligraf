import SwiftUI
import SharedLogic

struct RecordingView: View {
    @ObservedObject var navigator: IosNavigator
    let component: RecordingComponent
    let designSystem: DesignSystem
    
    @ObservedObject var state: SKIEStateObserver<RecordingState>
    @State private var showSettings = false
    @Namespace private var bottomPanelNamespace

    init(navigator: IosNavigator, component: RecordingComponent, designSystem: DesignSystem) {
        self.navigator = navigator
        self.component = component
        self.designSystem = designSystem
        self._state = ObservedObject(wrappedValue: SKIEStateObserver(component.viewModel.state))
    }

    var body: some View {
        AppScaffold(
            viewModel: component.viewModel,
            state: state.value,
            designSystem: designSystem,
            onRetry: { },
            onRefresh: { }
        ) {
            ZStack(alignment: .bottom) {
                // 1. Evidence List
                ScrollView {
                    LazyVStack(spacing: 12) {
                        // Materials Tags Header
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 8) {
                                ForEach(state.value.materials, id: \.id) { tag in
                                    MaterialTagChip(tag: tag, designSystem: designSystem) {
                                        component.onMaterialTagClicked(tagId: tag.id)
                                    }
                                }
                            }
                            .padding(.horizontal)
                        }
                        .padding(.vertical, 8)

                        ForEach(state.value.widgets, id: \.id) { widget in
                            WidgetView(
                                widget: widget,
                                designSystem: designSystem,
                                onAction: { _ in },
                                recordingComponent: component
                            )
                        }
                    }
                    .padding(.horizontal)
                    .padding(.top, 16)
                    .padding(.bottom, 140) // Space for bottom panel
                }
                .ignoresSafeArea(.container, edges: .bottom)
                
                // 2. Control Panel Island
                ZStack {
                    HStack(spacing: 24) {
                        RecordingActionButton(systemName: designSystem.icon(.mic), designSystem: designSystem) {
                            withAnimation(.spring(response: 0.4, dampingFraction: 0.7)) {
                                component.onMicClicked()
                            }
                        }
                        RecordingActionButton(systemName: designSystem.icon(.gallery), designSystem: designSystem) { }
                        RecordingActionButton(systemName: designSystem.icon(.note), designSystem: designSystem) { }
                    }
                    .padding(8)
                    .matchedGeometryEffect(id: "content", in: bottomPanelNamespace)
                }
                .background(
                    RoundedRectangle(cornerRadius: 40)
                        .fill(.ultraThinMaterial)
                        .overlay(
                            RoundedRectangle(cornerRadius: 40)
                                .stroke(designSystem.color(.glassBorder).opacity(0.15), lineWidth: 0.5)
                        )
                        .matchedGeometryEffect(id: "island", in: bottomPanelNamespace)
                )
                .padding(.horizontal, 16)
                .padding(.bottom, 24)
            }
        }
        .navigationTitle("") // Keep back button clean (no text)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .principal) {
                HStack(spacing: 8) {
                    Text(state.value.subject.avatar)
                    
                    Text(state.value.subject.name)
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundColor(designSystem.color(.textPrimary))
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                .background(designSystem.color(.glassBase))
                .clipShape(Capsule())
                .overlay(Capsule().stroke(designSystem.color(.glassBorder), lineWidth: 0.5))
            }
            
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: { showSettings = true }) {
                    Image(systemName: designSystem.icon(.settings))
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(designSystem.color(.textPrimary))
                        .frame(width: 38, height: 38)
                        .clipShape(Circle())
                }
            }
        }
        .sheet(isPresented: $showSettings) {
            AppSheetContainer(
                designSystem: designSystem,
                titleToken: .drawerSettings,
                onUserClose: { showSettings = false }
            ) {
                List {
                    Button(role: .destructive, action: { 
                        showSettings = false
                        component.deleteRecording() 
                    }) {
                        HStack {
                            Image(systemName: designSystem.icon(.close))
                            Text(designSystem.string(token: .actionDeleteRecording))
                        }
                        .foregroundColor(designSystem.color(.error))
                    }
                    .listRowBackground(designSystem.color(.glassBase).opacity(0.1))
                }
                .scrollContentBackground(.hidden)
            }
            .presentationDetents([.medium])
            .presentationDragIndicator(.visible)
            .presentationBackground(designSystem.color(.surface).opacity(0.8))
        }
    }
}

struct MaterialTagChip: View {
    let tag: MaterialTag
    let designSystem: DesignSystem
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack(spacing: 4) {
                if let icon = tag.icon {
                    Text(icon)
                }
                Text(tag.title)
                    .font(.subheadline)
                    .fontWeight(.medium)
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 8)
            .background(Color.white.opacity(0.05))
            .foregroundColor(.white)
            .clipShape(Capsule())
            .overlay(Capsule().stroke(Color.white.opacity(0.1), lineWidth: 0.5))
        }
    }
}

struct RecordingActionButton: View {
    let systemName: String
    let designSystem: DesignSystem
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            Image(systemName: systemName)
                .font(.title3)
                .foregroundColor(designSystem.color(.textInverted))
                .frame(width: 52, height: 52)
                .background(designSystem.color(.accentPrimary))
                .clipShape(Circle())
        }
    }
}
