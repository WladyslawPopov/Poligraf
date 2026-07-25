import SwiftUI
import SharedLogic

struct MainView: View {
    @Environment(\.horizontalSizeClass) var sizeClass
    @ObservedObject var navigator: IosNavigator
    let component: MainComponent
    let designSystem: DesignSystem
    
    @ObservedObject var state: SKIEStateObserver<MainState>
    
    // Bridging common states for AppScaffold
    @ObservedObject var loading: SKIEStateObserver<KotlinBoolean>
    @ObservedObject var error: SKIEOptionalStateObserver<ErrorType>
    @ObservedObject var toast: SKIEOptionalStateObserver<ToastState>

    init(navigator: IosNavigator, component: MainComponent, designSystem: DesignSystem) {
        self.navigator = navigator
        self.component = component
        self.designSystem = designSystem
        self._state = ObservedObject(wrappedValue: SKIEStateObserver(component.viewModel.state))
        
        // Bridging BaseViewModel states to SwiftUI
        self._loading = ObservedObject(wrappedValue: SKIEStateObserver(component.viewModel.isLoading))
        self._error = ObservedObject(wrappedValue: SKIEOptionalStateObserver(component.viewModel.errorType))
        self._toast = ObservedObject(wrappedValue: SKIEOptionalStateObserver(component.viewModel.toastState))
    }

    var body: some View {
        AppScaffold(
            isLoading: loading.value.boolValue,
            errorType: error.value,
            toastState: toast.value,
            designSystem: designSystem,
            onRetry: { component.retry() },
            onRefresh: { component.retry() },
            onClearError: { component.viewModel.clearError() },
            onClearToast: { component.viewModel.clearToast() }
        ) {
            mainContent
        }
    }

    private var mainContent: some View {
        ScrollView {
            VStack {
                widgetList
                    .frame(maxWidth: sizeClass == .compact ? .infinity : CGFloat(designSystem.dimen(token: .maxContentWidth)))
            }
            .frame(maxWidth: .infinity)
        }
        .refreshable {
            component.retry()
        }
        .scrollContentBackground(.hidden)
        .navigationTitle(navigationTitle)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            navigationToolbar
        }
        .sheet(isPresented: $navigator.isDrawerOpen) {
            drawerSheet
        }
    }

    private var widgetList: some View {
        LazyVStack(spacing: CGFloat(designSystem.dimen(token: .widgetSpacing))) {
            Spacer().frame(height: CGFloat(designSystem.dimen(token: .spacingLarge)))
            
            ForEach(state.value.widgets, id: \.id) { widget in
                WidgetView(widget: widget, designSystem: designSystem, onAction: { component.onAction(action: $0) })
            }
        }
    }

    private var navigationTitle: String {
        let topBar = state.value.topBarState
        return topBar.titleToken.map { designSystem.string(token: $0) } ?? topBar.titleRaw ?? ""
    }

    @ToolbarContentBuilder
    private var navigationToolbar: some ToolbarContent {
        if sizeClass == .compact && navigator.path.isEmpty {
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: { navigator.toggleDrawer() }) {
                    Image(systemName: designSystem.icon(token: .menu))
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(IosTheme.color(.textPrimary, from: designSystem))
                }
            }
        }
    }

    private var drawerSheet: some View {
        DrawerView(navigator: navigator, designSystem: designSystem, onUserClose: {
            navigator.setDrawerOpen(isOpen: false)
        })
        .environment(\.colorScheme, navigator.isDark ? .dark : .light)
        .presentationDetents([.medium, .large])
        .presentationDragIndicator(.visible)
        .presentationBackground {
            IosTheme.color(.surface, from: designSystem)
                .opacity(0.8)
                .background(.ultraThinMaterial)
        }
    }
}
