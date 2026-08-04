package application.liedetector.ui.screens.debug.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import application.liedetector.presentation.debug.DebugComponent
import application.liedetector.theme.utils.composeColor
import application.liedetector.uicore.theme.LocalDesignSystem
import application.liedetector.uicore.theme.tokens.ColorToken
import application.liedetector.uicore.theme.tokens.DimenToken
import application.liedetector.uicore.theme.tokens.StringToken
import application.liedetector.uicore.actions.DebugAction

@Composable
fun StatesTab(
    component: DebugComponent,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val designSystem = LocalDesignSystem.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(designSystem.dimen(DimenToken.SPACING_MEDIUM).dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DebugSection(title = "Engine States") {
            DebugActionButton(
                text = designSystem.string(StringToken.DEBUG_TRIGGER_LOADING),
                onClick = { component.onAction(DebugAction.TriggerLoading) },
                color = ColorToken.ACCENT_PRIMARY
            )
            DebugActionButton(
                text = designSystem.string(StringToken.DEBUG_TRIGGER_ERROR_BLOCKING),
                onClick = { component.onAction(DebugAction.TriggerErrorBlocking) },
                color = ColorToken.STRESS
            )
        }

        DebugSection(title = "Notifications / Toasts") {
            DebugActionButton(
                text = designSystem.string(StringToken.DEBUG_TRIGGER_ERROR_TOAST),
                onClick = { component.onAction(DebugAction.TriggerErrorNonBlocking) },
                color = ColorToken.ERROR
            )
            DebugActionButton(
                text = designSystem.string(StringToken.DEBUG_TRIGGER_SUCCESS_TOAST),
                onClick = { component.onAction(DebugAction.TriggerSuccessToast) },
                color = ColorToken.TRUTH
            )
        }
    }
}

@Composable
private fun DebugSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val designSystem = LocalDesignSystem.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(designSystem.composeColor(ColorToken.SURFACE_VARIANT).copy(alpha = 0.4f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = designSystem.composeColor(ColorToken.TEXT_SECONDARY),
            fontWeight = FontWeight.Bold
        )
        content()
    }
}

@Composable
private fun DebugActionButton(
    text: String,
    onClick: () -> Unit,
    color: ColorToken
) {
    val designSystem = LocalDesignSystem.current
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = designSystem.composeColor(color).copy(alpha = 0.8f),
            contentColor = designSystem.composeColor(ColorToken.TEXT_INVERTED)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(text = text)
    }
}
