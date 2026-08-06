import SwiftUI
import SharedLogic

struct MainView: View {
    @Environment(\.horizontalSizeClass) var sizeClass
    @ObservedObject var navigator: IosNavigator
    let component: MainComponent
    let designSystem: DesignSystem
    
    @ObservedObject var state: SKIEStateObserver<MainState>
    
    @ObservedObject var loading: SKIEStateObserver<KotlinBoolean>
    @ObservedObject var error: SKIEOptionalStateObserver<ErrorType>
    @ObservedObject var toast: SKIEOptionalStateObserver<ToastState>

    @State private var contentVisible: Bool = false

    init(navigator: IosNavigator, component: MainComponent, designSystem: DesignSystem) {
        self.navigator = navigator
        self.component = component
        self.designSystem = designSystem
        self._state = ObservedObject(wrappedValue: SKIEStateObserver(component.viewModel.state))
        
        self._loading = ObservedObject(wrappedValue: SKIEStateObserver(component.viewModel.isLoading))
        self._error = ObservedObject(wrappedValue: SKIEOptionalStateObserver(component.viewModel.errorType))
        self._toast = ObservedObject(wrappedValue: SKIEOptionalStateObserver(component.viewModel.toastState))
    }

    var body: some View {
        AppScaffold(
            viewModel: component.viewModel,
            state: state.value,
            designSystem: designSystem,
            onRetry: { component.retry() },
            onRefresh: { component.retry() }
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
        ScrollView {
            VStack(spacing: 0) {
                widgetList
            }
        }
        .refreshable {
            component.retry()
        }
        .scrollContentBackground(.hidden)
        .navigationBarTitleDisplayMode(.inline)
        .sheet(isPresented: $navigator.isDrawerOpen) {
            drawerSheet
        }
    }

    private var widgetList: some View {
        LazyVStack(spacing: CGFloat(designSystem.dimen(token: .widgetSpacing))) {
            if let welcome = state.value.welcomeWidget {
                WidgetView(widget: welcome, designSystem: designSystem, onAction: { component.onAction(action: $0) })
            }
            
            if contentVisible {
                ForEach(state.value.widgets, id: \.id) { widget in
                    WidgetView(widget: widget, designSystem: designSystem, onAction: { component.onAction(action: $0) })
                }
                .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
    }

    @ToolbarContentBuilder
    private var navigationToolbar: some ToolbarContent {
        let tb = state.value.toolbar
        
        ToolbarItem(placement: .navigationBarLeading) {
            if let tb = tb {
                Button(action: { component.onAction(action: tb.menuAction) }) {
                    Image(systemName: designSystem.icon(token: .menu))
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(IosTheme.color(tb.contentColor, from: designSystem))
                }
            }
        }
        
        ToolbarItem(placement: .principal) {
            VStack(spacing: 2) {
                if let titleToken = tb?.titleToken {
                    Text(designSystem.string(token: titleToken))
                        .font(.headline)
                        .foregroundColor(IosTheme.color(tb?.contentColor ?? .textPrimary, from: designSystem))
                }
                if let subtitleToken = tb?.subtitleToken {
                    Text(designSystem.string(token: subtitleToken))
                        .font(.caption2)
                        .foregroundColor(IosTheme.color(.textSecondary, from: designSystem))
                }
            }
            .id("toolbar_content_\(tb?.id ?? "none")_\(tb?.titleToken?.name ?? "none")")
        }
        
        ToolbarItem(placement: .navigationBarTrailing) {
            if let tb = tb {
                Button(action: { component.onAction(action: tb.profileAction) }) {
                    Image(systemName: designSystem.icon(token: .profile))
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(IosTheme.color(tb.contentColor, from: designSystem))
                }
            }
        }
    }

    private var drawerSheet: some View {
        DrawerView(
            navigator: navigator,
            designSystem: designSystem,
            appConfig: state.value.appConfig,
            onUserClose: {
                navigator.setDrawerOpen(isOpen: false)
            }
        )
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
