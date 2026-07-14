package application.liedetector.presentation.investigation

import application.liedetector.navigation.NavigationContext
import application.liedetector.presentation.base.BaseViewModel
import application.liedetector.uiwidgets.models.WidgetDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InvestigationComponent(
    val subjectId: String,
    val context: NavigationContext,
    val viewModel: InvestigationViewModel
)

data class InvestigationState(
    val widgets: List<WidgetDto> = emptyList(),
    val isRecording: Boolean = false,
    val isSubjectInfoExpanded: Boolean = false
)

class InvestigationViewModel(private val subjectId: String) : BaseViewModel() {
    private val _state = MutableStateFlow(InvestigationState())
    val state: StateFlow<InvestigationState> = _state.asStateFlow()

    fun loadInvestigation() {
        // Load chat history for specific subject
    }

    fun toggleSubjectInfo() {
        _state.value = _state.value.copy(isSubjectInfoExpanded = !_state.value.isSubjectInfoExpanded)
    }
}
