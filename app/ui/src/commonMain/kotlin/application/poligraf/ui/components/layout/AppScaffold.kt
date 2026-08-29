package application.poligraf.ui.components.layout

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
import application.poligraf.ui.features.background.ScalesBackground
import application.poligraf.ui.base.IBaseViewModel
import application.poligraf.ui.foundation.models.DisplayMetrics
import application.poligraf.ui.foundation.state.ScaffoldUiState
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.DimenToken
import application.poligraf.ui.foundation.types.ContentPaddingType
import application.poligraf.ui.foundation.types.ToastType
import application.poligraf.ui.foundation.models.AppBackground
import application.poligraf.ui.components.status.AppSnackBar
import application.poligraf.ui.components.status.ErrorView
import application.poligraf.ui.components.status.LoadingView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    viewModel: IBaseViewModel,
    state: ScaffoldUiState,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {},
    onRefresh: (() -> Unit)? = null,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
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
                val message = s.provider?.let { it(designSystem.strings) } ?: s.messageRaw ?: ""
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short,
                    withDismissAction = true
                )
                viewModel.clearToast()
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (val bg = state.background) {
                is AppBackground.AnimatedScales -> {
                    ScalesBackground(config = bg)
                }
                is AppBackground.Solid -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(designSystem.color(bg.colorToken))
                    )
                }
            }

            Scaffold(
                containerColor = Color.Transparent,
                topBar = topBar,
                bottomBar = bottomBar,
                floatingActionButton = floatingActionButton,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                val spacingMedium = designSystem.dimen(DimenToken.SPACING_MEDIUM)
                
                val contentPadding = when (layout.contentPaddingType) {
                    ContentPaddingType.NONE -> PaddingValues(0.dp)
                    ContentPaddingType.NORMAL -> padding
                    ContentPaddingType.LARGE -> PaddingValues(
                        start = padding.calculateStartPadding(LayoutDirection.Ltr) + spacingMedium,
                        end = padding.calculateEndPadding(LayoutDirection.Ltr) + spacingMedium,
                        top = padding.calculateTopPadding() + spacingMedium,
                        bottom = padding.calculateBottomPadding() + spacingMedium
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
                                Modifier.widthIn(max = designSystem.dimen(maxContent))
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
