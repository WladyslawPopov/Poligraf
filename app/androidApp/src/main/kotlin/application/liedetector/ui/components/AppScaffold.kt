package application.liedetector.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import application.liedetector.presentation.base.IBaseViewModel
import application.liedetector.ui.components.state.ErrorView
import application.liedetector.ui.components.state.LoadingView
import application.liedetector.ui.components.state.ToastView

/**
 * Universal Scaffold for LieDetector.
 * Automatically handles Loading, Error, and Toast states from the ViewModel.
 */
@Composable
fun AppScaffold(
    viewModel: IBaseViewModel,
    onRetry: () -> Unit = {},
    topBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val errorType by viewModel.errorType.collectAsState()
    val toastState by viewModel.toastState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = topBar,
            floatingActionButton = floatingActionButton,
            content = content
        )

        // Overlay states managed globally
        
        // 1. Loading Indicator (Non-blocking)
        LoadingView(isVisible = isLoading)

        // 2. Error Overlays (Dialog-based)
        errorType?.let { type ->
            ErrorView(
                type = type,
                onRetry = onRetry,
                onDismiss = { viewModel.clearError() }
            )
        }

        // 3. Toasts
        toastState?.let { state ->
            ToastView(
                state = state,
                onDismiss = { viewModel.clearToast() }
            )
        }
    }
}
