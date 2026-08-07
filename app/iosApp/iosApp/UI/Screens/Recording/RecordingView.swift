import SwiftUI
import SharedLogic

struct RecordingView: View {
    @ObservedObject var navigator: IosNavigator
    let component: RecordingComponent
    let designSystem: DesignSystem
    
    @ObservedObject var state: SKIEStateObserver<RecordingState>

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
                    Text(state.value.subject?.avatar ?? "🕵️")
                    Text(state.value.subject?.name ?? "Undefined-1")
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundColor(designSystem.color(.textPrimary))
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                .clipShape(Capsule())
            }
            
            ToolbarItem(placement: .navigationBarTrailing) {
                Menu {
                    Button(role: .destructive, action: { component.deleteRecording() }) {
                        Label("Delete Recording", systemImage: designSystem.icon(.close))
                    }
                } label: {
                    Image(systemName: designSystem.icon(.settings))
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(designSystem.color(.textPrimary))
                        .frame(width: 38, height: 38)
                        .clipShape(Circle())
                }
            }
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
