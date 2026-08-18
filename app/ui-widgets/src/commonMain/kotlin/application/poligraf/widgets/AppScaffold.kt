package application.poligraf.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import application.poligraf.widgets.background.ScalesBackground
import application.poligraf.widgets.state.AppSnackBar
import application.poligraf.widgets.state.ErrorView
import application.poligraf.uicore.base.IBaseViewModel
import application.poligraf.uicore.models.DisplayMetrics
import application.poligraf.uicore.state.ScaffoldUiState
import application.poligraf.uicore.theme.LocalDesignSystem
import application.poligraf.uicore.types.ContentPaddingType
import application.poligraf.uicore.types.ToastType
import application.poligraf.uicore.widgets.AppBackground
import application.poligraf.widgets.utils.composeColor
import application.poligraf.widgets.state.LoadingView

/**
 * Universal Scaffold for Poligraf.
 * Automatically handles Loading, Error, and Toast states from the ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    viewModel: IBaseViewModel,
    state: ScaffoldUiState,
    modifier: Modifier = Modifier,
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

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight
        
        // Update display metrics in ViewModel
        LaunchedEffect(maxWidth, maxHeight, isLandscape) {
            viewModel.setDisplayMetrics(
                DisplayMetrics(
                    isLandscape = isLandscape,
                    windowWidthPx = maxWidth.value.toInt(),
                    windowHeightPx = maxHeight.value.toInt()
                )
            )
        }

        LaunchedEffect(toastState) {
            toastState?.let { s ->
                val message = s.messageToken?.let { designSystem.string(it) } ?: s.messageRaw ?: ""
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short,
                    withDismissAction = true
                )
                viewModel.clearToast()
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            // Background Dispatcher
            when (val bg = state.background) {
                is AppBackground.AnimatedScales -> {
                    ScalesBackground(config = bg)
                }
                is AppBackground.Solid -> {
                    val bgColor: Color = designSystem.composeColor(bg.colorToken)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(bgColor)
                    )
                }
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
                val layout = state.layoutConfig
                val contentPadding = when (layout.contentPaddingType) {
                    ContentPaddingType.NONE -> PaddingValues(0.dp)
                    ContentPaddingType.NORMAL -> padding
                    ContentPaddingType.LARGE -> PaddingValues(
                        start = padding.calculateStartPadding(LayoutDirection.Ltr) + 16.dp,
                        end = padding.calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
                        top = padding.calculateTopPadding() + 16.dp,
                        bottom = padding.calculateBottomPadding() + 16.dp
                    )
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = if (layout.isCentered) Alignment.TopCenter else Alignment.TopStart
                ) {
                    val maxContent = layout.maxContentWidth
                    val contentModifier = Modifier
                        .fillMaxHeight()
                        .then(
                            if (maxContent != null) {
                                Modifier.widthIn(max = designSystem.dimen(maxContent).dp)
                            } else {
                                Modifier.fillMaxWidth()
                            }
                        )

                    Box(modifier = contentModifier) {
                        if (errorType != null) {
                            Box(modifier = Modifier.padding(padding)) {
                                ErrorView(
                                    type = errorType!!,
                                    onRetry = {
                                        viewModel.clearError()
                                        onRetry()
                                    }
                                )
                            }
                        } else if (onRefresh != null) {
                            val pullToRefreshState = rememberPullToRefreshState()
                            PullToRefreshBox(
                                isRefreshing = isLoading,
                                onRefresh = onRefresh,
                                state = pullToRefreshState,
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.TopCenter,
                                indicator = {
                                    PullToRefreshDefaults.Indicator(
                                        state = pullToRefreshState,
                                        isRefreshing = isLoading,
                                        modifier = Modifier
                                            .align(Alignment.TopCenter)
                                            .padding(top = padding.calculateTopPadding())
                                    )
                                }
                            ) {
                                content(contentPadding)
                            }
                        } else {
                            Box(modifier = Modifier.fillMaxSize()) {
                                content(contentPadding)
                                if (state.background !is AppBackground.AnimatedScales) {
                                    LoadingView(
                                        isVisible = isLoading,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(padding)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
