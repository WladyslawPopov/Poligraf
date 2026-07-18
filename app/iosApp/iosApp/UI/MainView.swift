import SwiftUI
import SharedLogic

struct MainView: View {
    @Environment(\.horizontalSizeClass) var sizeClass
    let component: MainComponent
    let designSystem: DesignSystem
    let navigator: IosNavigator<AnyObject>
    
    @ObservedObject var state: ObservableState<MainState>
    
    // Bridging common states for AppScaffold
    @ObservedObject var loading: ObservableState<KotlinBoolean>
    @ObservedObject var error: ObservableState<ErrorType>
    @ObservedObject var toast: ObservableState<ToastState>

    init(component: MainComponent, designSystem: DesignSystem, navigator: IosNavigator<AnyObject>) {
        self.component = component
        self.designSystem = designSystem
        self.navigator = navigator
        self._state = ObservedObject(wrappedValue: ObservableState<MainState>(component.stateWatcher))
        
        // Bridging BaseViewModel states to SwiftUI
        self._loading = ObservedObject(wrappedValue: ObservableState<KotlinBoolean>(component.viewModel.isLoadingWatcher))
        self._error = ObservedObject(wrappedValue: ObservableState<ErrorType>(component.viewModel.errorTypeWatcher))
        self._toast = ObservedObject(wrappedValue: ObservableState<ToastState>(component.viewModel.toastStateWatcher))
    }

    var body: some View {
        AppScaffold(
            isLoading: loading.value?.boolValue ?? false,
            errorType: error.value,
            toastState: toast.value,
            designSystem: designSystem,
            onRetry: { component.retry() },
            onClearError: { component.viewModel.clearError() },
            onClearToast: { component.viewModel.clearToast() }
        ) {
            ScrollView {
                VStack {
                    LazyVStack(spacing: CGFloat(designSystem.dimen(token: .widgetSpacing))) {
                        Spacer().frame(height: CGFloat(designSystem.dimen(token: .spacingLarge)))
                        
                        if let widgets = state.value?.widgets {
                            ForEach(widgets, id: \.id) { widget in
                                WidgetView(widget: widget, designSystem: designSystem, onAction: { component.onAction(action: $0) })
                            }
                        }
                    }
                    .frame(maxWidth: sizeClass == .compact ? .infinity : 600) // Cap widget width on iPad
                }
                .frame(maxWidth: .infinity) // Center the capped container
            }
            .scrollContentBackground(.hidden)
            .navigationTitle(state.value?.topBarState.titleToken.map { designSystem.string(token: $0) } ?? state.value?.topBarState.titleRaw ?? "")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                if sizeClass == .compact && navigator.path.isEmpty {
                    ToolbarItem(placement: .navigationBarLeading) {
                        Button(action: { navigator.toggleDrawer() }) {
                            Image(systemName: designSystem.icon(token: .menu))
                                .font(.system(size: 18, weight: .bold))
                                .foregroundColor(IosTheme.color(.textPrimary, from: designSystem))                        }
                    }
                }
            }
        }
    }
}
