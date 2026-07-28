package application.liedetector.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import application.liedetector.presentation.base.IBaseViewModel
import application.liedetector.theme.utils.composeColor
import application.liedetector.ui.components.background.ScalesBackground
import application.liedetector.ui.components.state.AppSnackBar
import application.liedetector.ui.components.state.ErrorView
import application.liedetector.ui.components.state.LoadingView
import application.liedetector.uicore.state.ToastType
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

    val snackbarHostState = remember { SnackbarHostState() }

    // Sync ViewModel ToastState to Material Snackbar
    LaunchedEffect(toastState) {
        toastState?.let { state ->
            val message = state.messageToken?.let { designSystem.string(it) } ?: state.messageRaw ?: ""
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short,
                withDismissAction = true
            )
            viewModel.clearToast()
        }
    }

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
            floatingActionButton = floatingActionButton,
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    AppSnackBar(
                        data = data,
                        type = toastState?.type ?: ToastType.WARNING
                    )
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (errorType != null) {
                    ErrorView(
                        type = errorType!!,
                        onRetry = {
                            viewModel.clearError()
                            onRetry()
                        }
                    )
                } else if (onRefresh != null) {
                    PullToRefreshBox(
                        isRefreshing = isLoading,
                        onRefresh = onRefresh,
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        content(PaddingValues(0.dp))
                    }
                } else {
                    content(PaddingValues(0.dp))
                    LoadingView(isVisible = isLoading)
                }
            }
        }
    }
}
