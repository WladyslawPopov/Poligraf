package application.poligraf.presentation.history

import application.poligraf.domain.history.repository.HistoryRepository
import application.poligraf.engine.utils.convertDateWithMinutes
import application.poligraf.presentation.base.BaseViewModel
import application.poligraf.ui.features.history.actions.HistoryAction
import application.poligraf.ui.features.history.state.HistoryState
import application.poligraf.ui.features.history.state.SessionUiModel
import application.poligraf.ui.foundation.models.AppBackground
import application.poligraf.ui.foundation.models.AppToolbar
import application.poligraf.ui.foundation.models.ToolbarAction
import application.poligraf.ui.foundation.types.BackgroundMode
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.IconToken
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
            toolbar = createDefaultToolbar()
        )
    )
    val state = _state.asStateFlow()

    init {
        scope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    background = createBackground(
                        isLoading = true,
                        isEmpty = it.sessions.isEmpty(),
                        isSelection = false
                    )
                )
            }
            historyRepository.getSessions().collect { sessions ->
                val uiModels = sessions.map { session ->
                    SessionUiModel(
                        id = session.id,
                        title = session.title.ifEmpty { "New Session" },
                        dateText = (session.timestamp / 1000).convertDateWithMinutes(),
                        durationMillis = session.duration,
                        markerCount = session.anomalyCount,
                        noteCount = session.noteCount,
                        timestamp = session.timestamp
                    )
                }
                _state.update {
                    it.copy(
                        sessions = uiModels,
                        isLoading = false,
                        background = createBackground(
                            isLoading = false,
                            isEmpty = uiModels.isEmpty(),
                            isSelection = it.selectedIds.isNotEmpty()
                        )
                    )
                }
            }
        }
    }

    private fun createBackground(
        isLoading: Boolean,
        isEmpty: Boolean,
        isSelection: Boolean,
    ): AppBackground {
        return when {
            isSelection -> AppBackground.AnimatedScales(
                mode = BackgroundMode.ERROR,
                energyColor = ColorToken.STATE_ERROR
            )

            isLoading -> AppBackground.AnimatedScales(
                mode = BackgroundMode.IDLE,
                energyColor = ColorToken.ACCENT_ENERGY
            )

            isEmpty -> AppBackground.AnimatedScales(
                mode = BackgroundMode.WAITING,
                energyColor = ColorToken.STATE_WARNING
            )

            else -> AppBackground.AnimatedScales(
                mode = BackgroundMode.WAITING, // Yin-Yang effect for items
                energyColor = ColorToken.ACCENT_ENERGY
            )
        }
    }

    private fun createDefaultToolbar() = AppToolbar(
        titleToken = StringToken.HISTORY,
        backgroundColor = ColorToken.SURFACE_BACKGROUND,
        contentColor = ColorToken.TEXT_PRIMARY
    )

    private fun createSelectionToolbar(count: Int) = AppToolbar(
        titleToken = StringToken.HISTORY,
        navigationIcon = IconToken.CLOSE,
        navigationAction = HistoryAction.ClearSelection,
        trailingActions = listOf(
            ToolbarAction(
                icon = IconToken.DELETE,
                action = HistoryAction.DeleteSelected,
                tint = ColorToken.STATE_ERROR
            )
        )
    )

    private fun updateStateWithSelection(selectedIds: Set<String>) {
        _state.update {
            it.copy(
                selectedIds = selectedIds,
                toolbar = if (selectedIds.isNotEmpty()) createSelectionToolbar(selectedIds.size) else createDefaultToolbar(),
                background = createBackground(
                    isLoading = it.isLoading,
                    isEmpty = it.sessions.isEmpty(),
                    isSelection = selectedIds.isNotEmpty()
                )
            )
        }
    }

    fun onSessionClick(sessionId: String) {
        if (_state.value.isSelectionMode) {
            toggleSelection(sessionId)
        } else {
            navigateToDetail(sessionId)
        }
    }

    fun onSessionLongClick(sessionId: String) {
        toggleSelection(sessionId)
    }

    private fun toggleSelection(sessionId: String) {
        val currentSelected = _state.value.selectedIds.toMutableSet()
        if (currentSelected.contains(sessionId)) {
            currentSelected.remove(sessionId)
        } else {
            currentSelected.add(sessionId)
        }
        updateStateWithSelection(currentSelected)
    }

    fun onAction(action: Any) {
        when (action) {
            is HistoryAction.ClearSelection -> {
                updateStateWithSelection(emptySet())
            }

            is HistoryAction.DeleteSelected -> {
                val idsToDelete = _state.value.selectedIds
                scope.launch {
                    idsToDelete.forEach { historyRepository.deleteSession(it) }
                    updateStateWithSelection(emptySet())
                }
            }

            is HistoryAction.ToggleSelection -> toggleSelection(action.id)
            else -> {}
        }
    }

    fun onDeleteSession(sessionId: String) {
        scope.launch {
            historyRepository.deleteSession(sessionId)
        }
    }

    fun onBack() {
        if (_state.value.isSelectionMode) {
            onAction(HistoryAction.ClearSelection)
        } else {
            navigateBack()
        }
    }
}
