import SwiftUI
import SharedLogic

struct DebugView: View {
    let component: DebugComponent
    let designSystem: DesignSystem
    
    @ObservedObject var state: ObservableState<DebugState>
    @ObservedObject var loading: ObservableState<KotlinBoolean>
    @ObservedObject var error: ObservableState<ErrorType>
    @ObservedObject var toast: ObservableState<ToastState>

    init(component: DebugComponent, designSystem: DesignSystem) {
        self.component = component
        self.designSystem = designSystem
        self.state = ObservableState<DebugState>(component.stateWatcher)
        self.loading = ObservableState<KotlinBoolean>(component.viewModel.isLoadingWatcher)
        self.error = ObservableState<ErrorType>(component.viewModel.errorTypeWatcher)
        self.toast = ObservableState<ToastState>(component.viewModel.toastStateWatcher)
    }

    var body: some View {
        AppScaffold(
            isLoading: loading.value?.boolValue ?? false,
            errorType: error.value,
            toastState: toast.value,
            designSystem: designSystem,
            onClearError: { component.viewModel.clearError() },
            onClearToast: { component.viewModel.clearToast() }
        ) {
            VStack(spacing: 0) {
                Picker("", selection: Binding(
                    get: { state.value?.selectedTab ?? .states },
                    set: { component.setTab(tab: $0) }
                )) {
                    Text(designSystem.string(token: .tabStates)).tag(DebugTab.states)
                    Text(designSystem.string(token: .tabWidgets)).tag(DebugTab.widgets)
                    Text(designSystem.string(token: .tabLabs)).tag(DebugTab.labs)
                }
                .pickerStyle(.segmented)
                .padding()
                
                contentView
            }
            .navigationTitle(designSystem.string(token: .debugDashboard))
            .navigationBarTitleDisplayMode(.inline)
        }
    }
    
    @ViewBuilder
    private var contentView: some View {
        switch state.value?.selectedTab {
        case .widgets:
            widgetsTab
        case .labs:
            labsTab
        default:
            statesTab
        }
    }
    
    private var statesTab: some View {
        VStack(spacing: 20) {
            Button(designSystem.string(token: .debugTriggerLoading)) {
                component.onAction(action: .debugTriggerLoading)
            }
            .buttonStyle(.borderedProminent)
            
            Button(designSystem.string(token: .debugTriggerErrorBlocking)) {
                component.onAction(action: .debugTriggerErrorBlocking)
            }
            .buttonStyle(.borderedProminent)
            
            Button(designSystem.string(token: .debugTriggerErrorToast)) {
                component.onAction(action: .debugTriggerErrorNonBlocking)
            }
            .buttonStyle(.borderedProminent)
            
            Button(designSystem.string(token: .debugTriggerSuccessToast)) {
                component.onAction(action: .debugTriggerSuccessToast)
            }
            .buttonStyle(.borderedProminent)
            
            Spacer()
        }
        .padding()
    }
    
    private var widgetsTab: some View {
        ScrollView {
            VStack {
                if let widgets = state.value?.widgets {
                    ForEach(widgets, id: \.id) { widget in
                        WidgetView(widget: widget, designSystem: designSystem, onAction: { component.onAction(action: $0) })
                    }
                }
            }
        }
    }
    
    private var labsTab: some View {
        VStack {
            Text("Experimental Features will appear here")
                .foregroundColor(IosTheme.color(.textSecondary, from: designSystem))
        }
        .frame(maxHeight: .infinity)
    }
}
