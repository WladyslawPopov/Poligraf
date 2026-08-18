package application.liedetector.widgets.background

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberTiltState(): Pair<Float, Float> {
    return remember { 0f to 0f }
}
