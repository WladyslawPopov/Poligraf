package application.poligraf.presentation.main

import androidx.compose.runtime.Stable
import application.poligraf.engine.component.ComponentContext
import application.poligraf.uicore.actions.WidgetAction

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
