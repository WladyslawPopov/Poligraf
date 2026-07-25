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
            widgetsTab
        case .labs:
            labsTab
        default:
            statesTab
        }
    }
    
    private var statesTab: some View {
        VStack(spacing: CGFloat(designSystem.dimen(token: .widgetSpacing))) {
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
        .padding(CGFloat(designSystem.dimen(token: .mainPadding)))
    }
    
    private var widgetsTab: some View {
        ScrollView {
            VStack {
                ForEach(state.value.widgets, id: \.id) { widget in
                    WidgetView(widget: widget, designSystem: designSystem, onAction: { component.onAction(action: $0) })
                }
            }
        }
    }
    
    private var labsTab: some View {
        VStack {
            Text(designSystem.string(token: .labsEmptyMessage))
                .foregroundColor(IosTheme.color(.textSecondary, from: designSystem))
        }
        .frame(maxHeight: .infinity)
    }
}
