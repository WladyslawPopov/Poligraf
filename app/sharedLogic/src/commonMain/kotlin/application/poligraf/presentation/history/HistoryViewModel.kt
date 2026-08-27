package application.poligraf.presentation.history

import application.poligraf.domain.repository.HistoryRepository
import application.poligraf.engine.utils.convertDateWithMinutes
import application.poligraf.presentation.base.BaseViewModel
import application.poligraf.presentation.history.data.HistoryState
import application.poligraf.presentation.history.data.SessionUiModel
import application.poligraf.ui.foundation.models.AppToolbar
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.StringToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val historyRepository: HistoryRepository,
    private val navigateToDetail: (String) -> Unit,
    private val navigateBack: () -> Unit,
) : BaseViewModel() {

    private val _state = MutableStateFlow(
        HistoryState(
            toolbar = AppToolbar(
                titleToken = StringToken.HISTORY,
                backgroundColor = ColorToken.SURFACE_BACKGROUND,
                contentColor = ColorToken.TEXT_PRIMARY
            )
        )
    )
    val state = _state.asStateFlow()

    init {
        scope.launch {
            _state.update { it.copy(isLoading = true) }
            historyRepository.getSessions().collect { sessions ->
                val uiModels = sessions.map { session ->
                    SessionUiModel(
                        id = session.id,
                        title = session.title.ifEmpty { "New Session" },
                        dateText = (session.timestamp / 1000).convertDateWithMinutes(),
                        durationMillis = session.duration,
                        markerCount = session.anomalyCount,
                        timestamp = session.timestamp
                    )
                }
                _state.update {
                    it.copy(
                        sessions = uiModels,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSessionClick(sessionId: String) {
        navigateToDetail(sessionId)
    }

    fun onDeleteSession(sessionId: String) {
        scope.launch {
            historyRepository.deleteSession(sessionId)
        }
    }

    fun onBack() {
        navigateBack()
    }
}
