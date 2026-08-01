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
                    .frame(maxWidth: layout.maxContentWidth != nil ? CGFloat(truncating: designSystem.dimen(token: layout.maxContentWidth!) as NSNumber) : .infinity)
                    .frame(maxWidth: .infinity, alignment: layout.isCentered ? .center : .leading)
            }
            
            LoadingView(isVisible: isLoading, designSystem: designSystem)
            
            if let toast = toastState {
                AppToast(
                    state: toast,
                    designSystem: designSystem,
                    onDismiss: onClearToast
                )
            }
        }
        .containerBackground(IosTheme.color(.background, from: designSystem), for: .navigation)
        .onAppear {
            updateMetrics()
        }
        .onChange(of: verticalSizeClass) { _ in updateMetrics() }
        .onChange(of: horizontalSizeClass) { _ in updateMetrics() }
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
                IosTheme.color(solid.colorToken, from: designSystem)
            } else {
                IosTheme.color(.background, from: designSystem)
            }
        }
    }
}
