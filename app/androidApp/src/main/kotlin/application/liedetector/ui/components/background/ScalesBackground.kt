package application.liedetector.ui.components.background

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.draw.blur
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import application.liedetector.theme.utils.composeColor
import application.liedetector.uicore.theme.tokens.DimenToken
import application.liedetector.uicore.theme.LocalDesignSystem
import application.liedetector.uicore.widgets.AppBackground
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sin

@Composable
fun ScalesBackground(
    config: AppBackground.AnimatedScales,
    modifier: Modifier = Modifier
) {
    val designSystem = LocalDesignSystem.current
    val context = LocalContext.current
    
    var tiltX by remember { mutableFloatStateOf(0f) }
    var tiltY by remember { mutableFloatStateOf(0f) }
    
    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    tiltX = event.values[0] * 2f
                    tiltY = event.values[1] * 2f
                } else {
                    tiltX = -event.values[0] / 9.81f
                    tiltY = event.values[1] / 9.81f
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }
    
    val smoothX by animateFloatAsState(
        targetValue = tiltX,
        animationSpec = spring(stiffness = 100f, dampingRatio = Spring.DampingRatioLowBouncy)
    )
    val smoothY by animateFloatAsState(
        targetValue = tiltY,
        animationSpec = spring(stiffness = 100f, dampingRatio = Spring.DampingRatioLowBouncy)
    )

    val infiniteTransition = rememberInfiniteTransition(label = "aura")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween((8000 / config.animationSpeed).toInt(), easing = LinearEasing)
        )
    )

    val bgColor = designSystem.composeColor(config.baseColor)
    val scaleColor = designSystem.composeColor(config.particleColor)
    val energyColor = designSystem.composeColor(config.energyColor)
    
    val baseParallax = designSystem.dimen(DimenToken.PARALLAX_INTENSITY)
    val parallaxIntensity = baseParallax * config.parallaxIntensity
    val cornerRadiusValue = designSystem.dimen(DimenToken.CORNER_RADIUS)

    val rows = 36
    val cols = 18

    Box(modifier = modifier.fillMaxSize().background(bgColor)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .blur(config.blurRadius.dp)
        ) {
            val cellW = size.width / cols
            val cellH = size.height / rows
            val px = smoothX * parallaxIntensity
            val py = smoothY * parallaxIntensity

            val rectSize = Size(cellW / 1.5f, cellH / 2.0f)
            val halfRectW = rectSize.width / 2
            val halfRectH = rectSize.height / 2

            // Expanded loop range to prevent dark edges during parallax
            for (r in -2..rows + 2) {
                val y = r * cellH + cellH / 2 + py
                val dyNorm = abs(y - size.height / 2) / (size.height * 0.5f)
                val dy4 = dyNorm * dyNorm * dyNorm * dyNorm

                for (c in -2..cols + 2) {
                    val x = c * cellW + cellW / 2 + px
                    val dxNorm = abs(x - size.width / 2) / (size.width * 0.5f)
                    val distMask = (dxNorm * dxNorm * dxNorm * dxNorm + dy4).pow(0.25f)
                    
                    val wave = sin(distMask * 10f - time).coerceIn(0f, 1f)
                    val energyIntensity = (distMask * 1.2f).coerceIn(0.2f, 1f)
                    val rectTopLeft = Offset(x - halfRectW, y - halfRectH)

                    // Draw Scale
                    drawRoundRect(
                        color = scaleColor,
                        topLeft = rectTopLeft,
                        size = rectSize,
                        cornerRadius = CornerRadius(cornerRadiusValue, cornerRadiusValue),
                        alpha = 0.3f
                    )

                    // Draw Energy
                    drawRoundRect(
                        color = energyColor.copy(alpha = energyIntensity * 0.4f * (0.3f + wave * 0.7f)),
                        topLeft = rectTopLeft,
                        size = rectSize,
                        cornerRadius = CornerRadius(cornerRadiusValue, cornerRadiusValue)
                    )
                }
            }
        }
    }
}
