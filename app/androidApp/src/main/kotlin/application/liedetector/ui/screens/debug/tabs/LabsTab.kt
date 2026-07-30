package application.liedetector.ui.screens.debug.tabs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import application.liedetector.theme.utils.composeColor
import application.liedetector.uicore.theme.LocalDesignSystem
import application.liedetector.uicore.theme.tokens.ColorToken
import application.liedetector.uicore.theme.tokens.StringToken

@Composable
fun LabsTab() {
    val designSystem = LocalDesignSystem.current
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            designSystem.string(StringToken.LABS_EMPTY_MESSAGE),
            color = designSystem.composeColor(ColorToken.TEXT_SECONDARY)
        )
    }
}
