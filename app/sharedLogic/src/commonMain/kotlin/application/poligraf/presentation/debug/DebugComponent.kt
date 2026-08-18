package application.poligraf.presentation.debug

import androidx.compose.runtime.Stable
import application.poligraf.engine.component.ComponentContext
import application.poligraf.presentation.debug.data.DebugTab
import application.poligraf.uicore.actions.WidgetAction

@Stable
class DebugComponent(
    val context: ComponentContext,
    val viewModel: DebugViewModel
) {
    fun onAction(action: WidgetAction) {
        viewModel.onWidgetAction(action)
    }
    
    fun setTab(tab: DebugTab) {
        viewModel.setTab(tab)
    }

    fun goBack() {
        viewModel.goBack()
    }
}
