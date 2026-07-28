package application.liedetector.presentation.main

import androidx.compose.runtime.Stable
import application.liedetector.uicore.state.TopBarUiState
import application.liedetector.uicore.theme.StringToken
import application.liedetector.uicore.widgets.UiWidget

@Stable
data class MainState(
    val widgets: List<UiWidget> = emptyList(),
    val topBarState: TopBarUiState = TopBarUiState(titleToken = StringToken.APP_NAME),
    val errorRaw: String? = null,
    val errorToken: StringToken? = null
)
