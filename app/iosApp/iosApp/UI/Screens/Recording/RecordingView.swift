import SwiftUI
import SharedLogic

struct RecordingView: View {
    @ObservedObject var navigator: IosNavigator
    let component: RecordingComponent
    let designSystem: DesignSystem
    
    @ObservedObject var state: SKIEStateObserver<RecordingState>
    @State private var showSettings = false

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
            VStack {
                // 1. Evidence List
                ScrollView {
                    LazyVStack(spacing: 12) {
                        ForEach(state.value.widgets, id: \.id) { widget in
                            WidgetView(widget: widget, designSystem: designSystem, onAction: { _ in })
                        }
                    }
                    .padding()
                }
                
                Spacer()
                
                // 2. Control Panel Island
                HStack(spacing: 24) {
                    RecordingActionButton(systemName: designSystem.icon(.mic), designSystem: designSystem)
                    RecordingActionButton(systemName: designSystem.icon(.gallery), designSystem: designSystem)
                    RecordingActionButton(systemName: designSystem.icon(.note), designSystem: designSystem)
                }
                .padding(8)
                .background(.ultraThinMaterial)
                .clipShape(Capsule())
                .padding(.bottom, 20)
            }
        }
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: { component.goBack() }) {
                    Image(systemName: designSystem.icon(.arrowBack))
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(designSystem.color(.textPrimary))
                        .frame(width: 38, height: 38)
                        .clipShape(Circle())
                }
            }
            
            ToolbarItem(placement: .principal) {
                HStack(spacing: 8) {
                    if let avatar = state.value.subject?.avatar {
                        Text(avatar)
                    }
                    Text(state.value.subject?.name ?? "")
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

struct RecordingActionButton: View {
    let systemName: String
    let designSystem: DesignSystem
    
    var body: some View {
        Button(action: { }) {
            Image(systemName: systemName)
                .font(.title3)
                .foregroundColor(designSystem.color(.textInverted))
                .frame(width: 52, height: 52)
                .background(designSystem.color(.accentPrimary))
                .clipShape(Circle())
        }
    }
}
