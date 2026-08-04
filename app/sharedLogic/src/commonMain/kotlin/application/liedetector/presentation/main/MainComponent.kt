package application.liedetector.presentation.main

import androidx.compose.runtime.Stable
import application.liedetector.component.ComponentContext
import application.liedetector.uicore.actions.WidgetAction

@Stable
class MainComponent(
    val context: ComponentContext,
    val viewModel: MainViewModel
) {
    fun onAction(action: WidgetAction) {
        viewModel.onWidgetAction(action)
    }
    
    fun retry() {
        viewModel.loadContent()
    }
}
