package application.liedetector.presentation.investigation

import androidx.compose.runtime.Stable
import application.liedetector.component.ComponentContext

@Stable
class InvestigationComponent(
    val context: ComponentContext,
    val viewModel: InvestigationViewModel
) {
    fun goBack() {
        viewModel.goBack()
    }

    fun deleteSubject() {
        viewModel.deleteSubject()
    }
}
