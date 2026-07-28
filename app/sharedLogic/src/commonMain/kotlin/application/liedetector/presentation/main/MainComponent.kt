package application.liedetector.presentation.main

import androidx.compose.runtime.Stable
import application.liedetector.engine.component.ComponentContext
import application.liedetector.uicore.types.WidgetAction

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
