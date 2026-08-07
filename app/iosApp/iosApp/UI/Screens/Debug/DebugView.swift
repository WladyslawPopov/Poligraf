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
            viewModel: component.viewModel,
            state: state.value,
            designSystem: designSystem
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
        GlassSegmentedTabRow(
            items: [DebugTab.states, DebugTab.widgets, DebugTab.labs],
            selection: Binding(
                get: { state.value.selectedTab },
                set: { component.setTab(tab: $0) }
            ),
            designSystem: designSystem,
            labelProvider: { tab in
                switch tab {
                case .states: return designSystem.string(token: .tabStates)
                case .widgets: return designSystem.string(token: .tabWidgets)
                case .labs: return designSystem.string(token: .tabLabs)
                }
            }
        )
    }
    
    @ViewBuilder
    private var contentView: some View {
        TabView(selection: Binding(
            get: { state.value.selectedTab },
            set: { component.setTab(tab: $0) }
        )) {
            StatesTab(component: component, designSystem: designSystem)
                .tag(DebugTab.states)
            
            WidgetsTab(widgets: state.value.widgets, component: component, designSystem: designSystem)
                .tag(DebugTab.widgets)
            
            LabsTab(designSystem: designSystem)
                .tag(DebugTab.labs)
        }
        .tabViewStyle(.page(indexDisplayMode: .never))
    }
}
