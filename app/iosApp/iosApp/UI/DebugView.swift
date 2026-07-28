import SwiftUI
import SharedLogic

struct DebugView: View {
    @ObservedObject var navigator: IosNavigator
    let component: DebugComponent
    let designSystem: DesignSystem
    
    @ObservedObject var state: SKIEStateObserver<DebugState>
    @ObservedObject var loading: SKIEStateObserver<KotlinBoolean>
    @ObservedObject var error: SKIEOptionalStateObserver<ErrorType>
    @ObservedObject var toast: SKIEOptionalStateObserver<ToastState>

    init(navigator: IosNavigator, component: DebugComponent, designSystem: DesignSystem) {
        self.navigator = navigator
        self.component = component
        self.designSystem = designSystem
        self.state = SKIEStateObserver(component.viewModel.state)
        self.loading = SKIEStateObserver(component.viewModel.isLoading)
        self.error = SKIEOptionalStateObserver(component.viewModel.errorType)
        self.toast = SKIEOptionalStateObserver(component.viewModel.toastState)
    }

    var body: some View {
        AppScaffold(
            isLoading: loading.value.boolValue,
            errorType: error.value,
            toastState: toast.value,
            designSystem: designSystem,
            onClearError: { component.viewModel.clearError() },
            onClearToast: { component.viewModel.clearToast() }
        ) {
            VStack(spacing: 0) {
                tabPicker
                
                contentView
            }
            .navigationTitle(designSystem.string(token: .debugDashboard))
            .navigationBarTitleDisplayMode(.inline)
        }
    }

    private var tabPicker: some View {
        Picker("", selection: Binding(
            get: { state.value.selectedTab },
            set: { component.setTab(tab: $0) }
        )) {
            Text(designSystem.string(token: .tabStates)).tag(DebugTab.states)
            Text(designSystem.string(token: .tabWidgets)).tag(DebugTab.widgets)
            Text(designSystem.string(token: .tabLabs)).tag(DebugTab.labs)
        }
        .pickerStyle(.segmented)
        .padding()
    }
    
    @ViewBuilder
    private var contentView: some View {
        switch state.value.selectedTab {
        case .widgets:
            WidgetsTab(widgets: state.value.widgets, component: component, designSystem: designSystem)
        case .labs:
            LabsTab(designSystem: designSystem)
        default:
            StatesTab(component: component, designSystem: designSystem)
        }
    }
}
