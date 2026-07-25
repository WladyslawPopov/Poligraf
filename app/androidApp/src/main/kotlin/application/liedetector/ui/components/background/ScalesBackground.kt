package application.liedetector.ui.components.background

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import application.liedetector.uicore.theme.ColorToken
import application.liedetector.uicore.theme.DimenToken
import application.liedetector.uicore.theme.LocalDesignSystem
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sin

@Composable
fun ScalesBackground(
    modifier: Modifier = Modifier
) {
    val designSystem = LocalDesignSystem.current
    val context = LocalContext.current
    
    // Native Sensor State
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
    
    // Smooth tilt for parallax
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
        animationSpec = infiniteRepeatable(animation = tween(8000, easing = LinearEasing))
    )

    val bgColor = designSystem.composeColor(ColorToken.BACKGROUND)
    val scaleColor = designSystem.composeColor(ColorToken.SURFACE_VARIANT)
    val energyColor = designSystem.composeColor(ColorToken.ACCENT_ENERGY)
    val parallaxIntensity = designSystem.dimen(DimenToken.PARALLAX_INTENSITY)
    val cornerRadius = designSystem.dimen(DimenToken.CORNER_RADIUS)

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .blur(8.dp) // Integrated matte glass effect
    ) {
        drawRect(bgColor)
        
        // Add a subtle "fog" layer inside the canvas for better matte feel
        drawRect(
            color = bgColor.copy(alpha = 0.4f),
            size = size
        )
        
        val rows = 36
        val cols = 18
        val cellW = size.width / cols
        val cellH = size.height / rows

        // Parallax shift based on tilt
        val parallaxOffsetX = smoothX * parallaxIntensity
        val parallaxOffsetY = smoothY * parallaxIntensity

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                // Base position with parallax shift
                val basePos = Offset(
                    c * cellW + cellW / 2 + parallaxOffsetX, 
                    r * cellH + cellH / 2 + parallaxOffsetY
                )
                
                val dxNorm = abs(basePos.x - size.width / 2) / (size.width * 0.5f)
                val dyNorm = abs(basePos.y - size.height / 2) / (size.height * 0.5f)
                val distMask = (dxNorm.pow(4) + dyNorm.pow(4)).pow(0.25f)
                
                val wave = sin(distMask * 10f - time).coerceIn(0f, 1f)
                val energyIntensity = (distMask * 1.2f).coerceIn(0.2f, 1f)

                // Particles are static but shifted by parallax
                drawRoundRect(
                    color = scaleColor,
                    topLeft = Offset(basePos.x - cellW / 3, basePos.y - cellH / 4),
                    size = Size(cellW / 1.5f, cellH / 2f),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    alpha = 0.3f
                )

                // Energy Glow
                drawRoundRect(
                    color = energyColor.copy(alpha = energyIntensity * 0.3f * (0.3f + wave * 0.7f)),
                    topLeft = Offset(basePos.x - cellW / 3, basePos.y - cellH / 4),
                    size = Size(cellW / 1.5f, cellH / 2f),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                )
            }
        }
    }
}
