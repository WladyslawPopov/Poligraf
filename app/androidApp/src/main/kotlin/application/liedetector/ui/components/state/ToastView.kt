package application.liedetector.ui.components.state

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import application.liedetector.theme.utils.composeColor
import application.liedetector.uicore.state.*
import application.liedetector.uicore.theme.*
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ToastView(
    state: ToastState,
    onDismiss: () -> Unit
) {
    val designSystem = LocalDesignSystem.current

    val bgColor = when (state.type) {
        ToastType.SUCCESS -> designSystem.composeColor(ColorToken.TRUTH)
        ToastType.ERROR -> designSystem.composeColor(ColorToken.STRESS)
        ToastType.WARNING -> designSystem.composeColor(ColorToken.PRIMARY)
    }

    val textColor = when (state.type) {
        ToastType.SUCCESS, ToastType.ERROR -> designSystem.composeColor(ColorToken.TEXT_INVERTED)
        ToastType.WARNING -> designSystem.composeColor(ColorToken.TEXT_PRIMARY)
    }

    val message = state.messageToken?.let { designSystem.string(it) } ?: state.messageRaw ?: ""

    LaunchedEffect(state) {
        delay(3000.milliseconds)
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(designSystem.dimen(DimenToken.SPACING_LARGE).dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(designSystem.dimen(DimenToken.CORNER_RADIUS).dp))
                .background(bgColor)
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = message,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
