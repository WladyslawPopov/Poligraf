package application.poligraf.widgets.background

import androidx.compose.runtime.Composable

@Composable
expect fun rememberTiltState(): Pair<Float, Float>
