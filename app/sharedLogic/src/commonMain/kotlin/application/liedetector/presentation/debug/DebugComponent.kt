package application.liedetector.presentation.debug

import androidx.compose.runtime.Stable
import application.liedetector.component.ComponentContext
import application.liedetector.presentation.debug.data.DebugTab
import application.liedetector.uicore.types.WidgetAction

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
