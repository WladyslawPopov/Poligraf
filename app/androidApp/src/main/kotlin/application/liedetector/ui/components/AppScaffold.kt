package application.liedetector.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import application.liedetector.presentation.base.IBaseViewModel
import application.liedetector.theme.utils.composeColor
import application.liedetector.ui.components.background.ScalesBackground
import application.liedetector.ui.components.state.ErrorView
import application.liedetector.ui.components.state.LoadingView
import application.liedetector.ui.components.state.ToastView
import application.liedetector.uicore.theme.*

/**
 * Universal Scaffold for LieDetector.
 * Automatically handles Loading, Error, and Toast states from the ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    viewModel: IBaseViewModel,
    modifier: Modifier = Modifier,
    useAnimatedBackground: Boolean = true,
    onRetry: () -> Unit = {},
    onRefresh: (() -> Unit)? = null,
    topBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val errorType by viewModel.errorType.collectAsState()
    val toastState by viewModel.toastState.collectAsState()
    val designSystem = LocalDesignSystem.current

    Box(modifier = modifier.fillMaxSize()) {
        // 0. Background Layer
        if (useAnimatedBackground) {
            ScalesBackground()
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(designSystem.composeColor(ColorToken.BACKGROUND))
            )
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = topBar,
            floatingActionButton = floatingActionButton
        ) { padding ->
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(padding)
            ) {
                if (onRefresh != null) {
                    PullToRefreshBox(
                        isRefreshing = isLoading,
                        onRefresh = onRefresh,
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        content(PaddingValues(0.dp)) // Padding is already applied to the parent Box
                    }
                } else {
                    content(PaddingValues(0.dp))
                    LoadingView(isVisible = isLoading)
                }
            }
        }

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
