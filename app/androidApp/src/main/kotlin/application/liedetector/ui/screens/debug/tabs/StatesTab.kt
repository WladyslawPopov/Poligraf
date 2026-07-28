package application.liedetector.ui.screens.debug.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import application.liedetector.presentation.debug.DebugComponent
import application.liedetector.theme.utils.composeColor
import application.liedetector.uicore.theme.ColorToken
import application.liedetector.uicore.theme.LocalDesignSystem
import application.liedetector.uicore.theme.StringToken
import application.liedetector.uicore.types.WidgetAction

@Composable
fun StatesTab(component: DebugComponent) {
    val designSystem = LocalDesignSystem.current
    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = designSystem.composeColor(ColorToken.ACCENT_PRIMARY),
        contentColor = designSystem.composeColor(ColorToken.TEXT_INVERTED)
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { component.onAction(WidgetAction.DEBUG_TRIGGER_LOADING) },
            colors = buttonColors
        ) {
            Text(designSystem.string(StringToken.DEBUG_TRIGGER_LOADING))
        }
        Button(
            onClick = { component.onAction(WidgetAction.DEBUG_TRIGGER_ERROR_BLOCKING) },
            colors = buttonColors
        ) {
            Text(designSystem.string(StringToken.DEBUG_TRIGGER_ERROR_BLOCKING))
        }
        Button(
            onClick = { component.onAction(WidgetAction.DEBUG_TRIGGER_ERROR_NON_BLOCKING) },
            colors = buttonColors
        ) {
            Text(designSystem.string(StringToken.DEBUG_TRIGGER_ERROR_TOAST))
        }
        Button(
            onClick = { component.onAction(WidgetAction.DEBUG_TRIGGER_SUCCESS_TOAST) },
            colors = buttonColors
        ) {
            Text(designSystem.string(StringToken.DEBUG_TRIGGER_SUCCESS_TOAST))
        }
    }
}
