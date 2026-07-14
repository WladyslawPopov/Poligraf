package application.liedetector.ui.components.background

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import application.liedetector.theme.utils.composeColor
import application.liedetector.uicore.theme.BackgroundVisualizer
import application.liedetector.uicore.theme.ColorToken
import application.liedetector.uicore.theme.LocalDesignSystem
import org.koin.compose.koinInject
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sin

@Composable
fun ScalesBackground(
    modifier: Modifier = Modifier
) {
    val designSystem = LocalDesignSystem.current
    val visualizer: BackgroundVisualizer = koinInject()
    val state by visualizer.state.collectAsState()
    
    // Smooth tilt for rotation only
    val tx by animateFloatAsState(targetValue = state.tiltX, animationSpec = spring(stiffness = 200f))
    val ty by animateFloatAsState(targetValue = state.tiltY, animationSpec = spring(stiffness = 200f))

    // Wave cycle
    val infiniteTransition = rememberInfiniteTransition(label = "aura")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(animation = tween(5000, easing = LinearEasing))
    )

    val bgColor = designSystem.composeColor(ColorToken.BACKGROUND)
    val scaleColor = designSystem.composeColor(ColorToken.SURFACE_VARIANT)
    val energyColor = Color(0xFF00F2FF)

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(bgColor)
        
        val rows = 36
        val cols = 18
        val cellW = size.width / cols
        val cellH = size.height / rows

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val basePos = Offset(c * cellW + cellW / 2, r * cellH + cellH / 2)
                
                // 1. Rounded Rect Mask Calculation
                // We use a super-ellipse formula to get that smooth rounded-rect feel
                val dxNorm = abs(basePos.x - size.width / 2) / (size.width * 0.45f) // Increased center width
                val dyNorm = abs(basePos.y - size.height / 2) / (size.height * 0.45f) // Increased center height
                
                // Combined distance (the higher the power, the more rectangular the center)
                val distMask = (dxNorm.pow(4) + dyNorm.pow(4)).pow(0.25f)
                
                // 2. Rotation & Subtle Parallax (Minimized side-shift as requested)
                val rotation = (tx + ty) * 20f
                val ox = tx * 5f // Small parallax for 3D depth but not "shifting to sides"
                val oy = ty * 5f
                val drawCenter = Offset(basePos.x + ox, basePos.y + oy)

                // 3. Energy Logic: Dim in center, bright on edges
                val wave = sin(distMask * 8f - time).coerceIn(0f, 1f)
                
                // Base intensity: higher minimum (0.25f) to be seen through glass
                val energyIntensity = (distMask * 1.1f).coerceIn(0.25f, 1f)

                rotate(degrees = rotation, pivot = drawCenter) {
                    // Concrete Base
                    drawRoundRect(
                        color = scaleColor,
                        topLeft = Offset(drawCenter.x - cellW / 3, drawCenter.y - cellH / 4),
                        size = Size(cellW / 1.5f, cellH / 2f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f, 14f),
                        alpha = 0.5f // Increased alpha
                    )

                    // Energy Glow
                    drawRoundRect(
                        color = energyColor.copy(alpha = energyIntensity * 0.4f * (0.4f + wave * 0.6f)),
                        topLeft = Offset(drawCenter.x - cellW / 3, drawCenter.y - cellH / 4),
                        size = Size(cellW / 1.5f, cellH / 2f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f, 14f)
                    )
                }
            }
        }
    }
}
