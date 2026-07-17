import SwiftUI
import SharedLogic

/**
 * Universal wrapper for iOS screens. 
 * Automatically handles Loading, Error, and Toast states from the View Model.
 */
struct AppScaffold<Content: View>: View {
    let isLoading: Bool
    let errorType: ErrorType?
    let toastState: ToastState?
    let designSystem: DesignSystem
    let onRetry: () -> Void
    let onClearError: () -> Void
    let onClearToast: () -> Void
    let content: () -> Content

    init(
        viewModel: IBaseViewModel,
        designSystem: DesignSystem,
        onRetry: @escaping () -> Void = {},
        @ViewBuilder content: @escaping () -> Content
    ) {
        // Bridging StateFlow to simple Swift values for this wrapper
        // In a real reactive bridge, these would be Observed properties
        self.isLoading = viewModel.isLoading.value as? Bool ?? false
        self.errorType = viewModel.errorType.value as? ErrorType
        self.toastState = viewModel.toastState.value as? ToastState
        
        self.designSystem = designSystem
        self.onRetry = onRetry
        self.onClearError = { viewModel.clearError() }
        self.onClearToast = { viewModel.clearToast() }
        self.content = content
    }
    
    // Explicit init with direct values for better SwiftUI reactivity if needed
    init(
        isLoading: Bool,
        errorType: ErrorType?,
        toastState: ToastState?,
        designSystem: DesignSystem,
        onRetry: @escaping () -> Void = {},
        onClearError: @escaping () -> Void = {},
        onClearToast: @escaping () -> Void = {},
        @ViewBuilder content: @escaping () -> Content
    ) {
        self.isLoading = isLoading
        self.errorType = errorType
        self.toastState = toastState
        self.designSystem = designSystem
        self.onRetry = onRetry
        self.onClearError = onClearError
        self.onClearToast = onClearToast
        self.content = content
    }

    var body: some View {
        ZStack {
            // Main Layer
            content()
            
            // Overlay states
            
            // 1. Loading
            LoadingView(isVisible: isLoading, designSystem: designSystem)
            
            // 2. Error
            if let error = errorType {
                ErrorView(
                    type: error,
                    designSystem: designSystem,
                    onRetry: onRetry,
                    onDismiss: onClearError
                )
            }
            
            // 3. Toasts
            if let toast = toastState {
                ToastView(
                    state: toast,
                    designSystem: designSystem,
                    onDismiss: onClearToast
                )
            }
        }
    }
}
