import SwiftUI
import SharedLogic

struct AppScaffold<Content: View>: View {
    @Environment(\.verticalSizeClass) var verticalSizeClass
    @Environment(\.horizontalSizeClass) var horizontalSizeClass
    
    let viewModel: IBaseViewModel
    let isLoading: Bool
    let errorType: ErrorType?
    let toastState: ToastState?
    let designSystem: DesignSystem
    let state: ScaffoldUiState
    let onRetry: () -> Void
    let onRefresh: (() -> Void)?
    let onClearError: () -> Void
    let onClearToast: () -> Void
    let content: () -> Content

    init(
        viewModel: IBaseViewModel,
        state: ScaffoldUiState,
        designSystem: DesignSystem,
        onRetry: @escaping () -> Void = {},
        onRefresh: (() -> Void)? = nil,
        @ViewBuilder content: @escaping () -> Content
    ) {
        self.viewModel = viewModel
        self.isLoading = viewModel.isLoading.value.boolValue
        self.errorType = viewModel.errorType.value
        self.toastState = viewModel.toastState.value
        self.state = state
        self.designSystem = designSystem
        self.onRetry = onRetry
        self.onRefresh = onRefresh
        self.onClearError = { viewModel.clearError() }
        self.onClearToast = { viewModel.clearToast() }
        self.content = content
    }

    var body: some View {
        ZStack {
            // Background Dispatcher
            BackgroundView(background: state.background, designSystem: designSystem)
                .ignoresSafeArea()

            // Main Layer
            if let error = errorType {
                ErrorView(
                    type: error,
                    designSystem: designSystem,
                    onRetry: {
                        onClearError()
                        onRetry()
                    }
                )
            } else {
                let layout = state.layoutConfig
                
                content()
                    .frame(maxWidth: layout.maxContentWidth != nil ? designSystem.dimen(layout.maxContentWidth!) : .infinity)
                    .frame(maxWidth: .infinity, alignment: layout.isCentered ? .center : .leading)
            }
            
            if !(state.background is AppBackground.AnimatedScales) {
                LoadingView(isVisible: isLoading, designSystem: designSystem)
            }
            
            if let toast = toastState {
                AppToast(
                    state: toast,
                    designSystem: designSystem,
                    onDismiss: onClearToast
                )
            }
        }
        .containerBackground(designSystem.color(.background), for: .navigation)
        .onAppear {
            updateMetrics()
        }
        .onChange(of: verticalSizeClass) { _, _ in updateMetrics() }
        .onChange(of: horizontalSizeClass) { _, _ in updateMetrics() }
    }
    
    private func updateMetrics() {
        let metrics = DisplayMetrics(
            isLandscape: verticalSizeClass == .compact,
            windowWidthPx: Int32(UIScreen.main.bounds.width),
            windowHeightPx: Int32(UIScreen.main.bounds.height)
        )
        viewModel.setDisplayMetrics(metrics: metrics)
    }
}

struct BackgroundView: View {
    let background: AppBackground
    let designSystem: DesignSystem
    
    var body: some View {
        Group {
            if let scales = background as? AppBackground.AnimatedScales {
                ScalesView(designSystem: designSystem, config: scales) 
            } else if let solid = background as? AppBackground.Solid {
                designSystem.color(solid.colorToken)
            } else {
                designSystem.color(.background)
            }
        }
    }
}
