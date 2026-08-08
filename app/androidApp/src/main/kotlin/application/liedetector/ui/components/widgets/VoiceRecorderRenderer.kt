package application.liedetector.ui.components.widgets

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import application.liedetector.uicore.theme.LocalDesignSystem
import application.liedetector.uicore.theme.tokens.ColorToken
import application.liedetector.uicore.theme.tokens.IconToken
import application.liedetector.uicore.widgets.UiWidget
import application.liedetector.theme.utils.composeColor
import application.liedetector.uicore.theme.DesignSystem
import kotlin.math.absoluteValue

@Composable
fun VoiceRecorderRenderer(
    widget: UiWidget.VoiceRecorder,
    modifier: Modifier = Modifier,
    onToggle: () -> Unit,
    onStop: () -> Unit
) {
    val designSystem = LocalDesignSystem.current
    val accentColor = designSystem.composeColor(ColorToken.ACCENT_PRIMARY)
    val glassColor = designSystem.composeColor(ColorToken.GLASS_BASE).copy(alpha = 0.5f)
    val textColor = designSystem.composeColor(ColorToken.TEXT_PRIMARY)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        color = glassColor,
        border = BorderStroke(
            1.dp,
            designSystem.composeColor(ColorToken.GLASS_BORDER).copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Status and Timer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(widget.status, designSystem)
                Text(
                    text = formatDuration(widget.durationMillis),
                    style = MaterialTheme.typography.headlineMedium,
                    color = textColor
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Waveform Visualization
            WaveformVisualizer(
                amplitudes = widget.amplitudes,
                color = accentColor,
                isRecording = widget.status == UiWidget.VoiceRecorder.Status.RECORDING
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Controls
            if (widget.status != UiWidget.VoiceRecorder.Status.FINISHED) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledIconButton(
                        onClick = onToggle,
                        modifier = Modifier.size(56.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = designSystem.composeColor(ColorToken.SURFACE_VARIANT)
                        )
                    ) {
                        Icon(
                            imageVector = if (widget.status == UiWidget.VoiceRecorder.Status.PAUSED) 
                                designSystem.icon(IconToken.PLAY) else designSystem.icon(IconToken.PAUSE),
                            contentDescription = null,
                            tint = designSystem.composeColor(ColorToken.TEXT_PRIMARY)
                        )
                    }

                    Button(
                        onClick = onStop,
                        modifier = Modifier.height(56.dp).padding(horizontal = 16.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Icon(
                            imageVector = designSystem.icon(IconToken.CHECK),
                            contentDescription = null,
                            tint = designSystem.composeColor(ColorToken.TEXT_INVERTED)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Finish",
                            style = MaterialTheme.typography.labelLarge,
                            color = designSystem.composeColor(ColorToken.TEXT_INVERTED)
                        )
                    }
                }
            } else {
                Text(
                    text = "Recording saved",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: UiWidget.VoiceRecorder.Status, designSystem: DesignSystem) {
    val color = when (status) {
        UiWidget.VoiceRecorder.Status.RECORDING -> designSystem.composeColor(ColorToken.ERROR)
        UiWidget.VoiceRecorder.Status.PAUSED -> designSystem.composeColor(ColorToken.WARNING)
        else -> designSystem.composeColor(ColorToken.TEXT_PRIMARY).copy(alpha = 0.5f)
    }
    
    val text = when (status) {
        UiWidget.VoiceRecorder.Status.RECORDING -> "Recording"
        UiWidget.VoiceRecorder.Status.PAUSED -> "Paused"
        UiWidget.VoiceRecorder.Status.FINISHED -> "Finished"
        else -> "Idle"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        if (status == UiWidget.VoiceRecorder.Status.RECORDING) {
            val infiniteTransition = rememberInfiniteTransition()
            val alpha by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 0.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = alpha))
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
private fun WaveformVisualizer(
    amplitudes: List<Float>,
    color: Color,
    isRecording: Boolean
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        val width = size.width
        val height = size.height
        val barWidth = 4.dp.toPx()
        val gap = 2.dp.toPx()
        val totalBarWidth = barWidth + gap
        val maxBars = (width / totalBarWidth).toInt()
        
        val visibleAmplitudes = amplitudes.takeLast(maxBars)
        
        visibleAmplitudes.forEachIndexed { index, amplitude ->
            val x = width - (visibleAmplitudes.size - index) * totalBarWidth
            val barHeight = (amplitude * height).coerceAtLeast(barWidth)
            val y = (height - barHeight) / 2
            
            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}

private fun formatDuration(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    return "%02d:%02d".format(minutes, seconds)
}
