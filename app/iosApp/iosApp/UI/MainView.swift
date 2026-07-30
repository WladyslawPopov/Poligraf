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

    private var navigationTitle: String {
        if let toolbar = state.value.toolbar {
            return toolbar.titleToken.map { designSystem.string(token: $0) } ?? ""
        }
        return ""
    }

    @ToolbarContentBuilder
    private var navigationToolbar: some ToolbarContent {
        if let toolbar = state.value.toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: { component.onAction(action: toolbar.menuAction) }) {
                    Image(systemName: designSystem.icon(token: .menu))
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(IosTheme.color(toolbar.contentColor, from: designSystem))
                }
            }
            
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: { component.onAction(action: toolbar.profileAction) }) {
                    Image(systemName: designSystem.icon(token: .profile))
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(IosTheme.color(toolbar.contentColor, from: designSystem))
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
