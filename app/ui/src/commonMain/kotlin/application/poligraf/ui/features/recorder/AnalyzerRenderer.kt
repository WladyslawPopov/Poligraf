package application.poligraf.ui.features.recorder

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.DimenToken
import application.poligraf.ui.theme.tokens.IconToken
import application.poligraf.ui.theme.tokens.StringToken
import application.poligraf.ui.foundation.models.UiWidget
import kotlin.math.sin

enum class AnalyzerSkin {
    STATE_MAP,
    VOICE_RIBBON,
    EQUALIZER,
    RINGS
}

@Composable
fun AnalyzerRenderer(
    widget: UiWidget.Analyzer,
    modifier: Modifier = Modifier
) {
    val designSystem = LocalDesignSystem.current
    var currentSkin by remember { mutableStateOf(AnalyzerSkin.STATE_MAP) }
    var isPaused by remember { mutableStateOf(false) }
    
    // Mock metrics
    val infiniteTransition = rememberInfiniteTransition(label = "metrics")
    val animFactor by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "animFactor"
    )

    val jitter = 18f + (animFactor * 5f)
    val pitch = 64f + (animFactor * 10f)
    val rms = 9f + (animFactor * 3f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(designSystem.color(ColorToken.SURFACE_BACKGROUND))
            .padding(designSystem.dimen(DimenToken.SPACING_LARGE)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(designSystem.color(ColorToken.STATE_ERROR), CircleShape))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = designSystem.string(StringToken.ACTIVE_SESSION), 
                        color = designSystem.color(ColorToken.TEXT_SECONDARY),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Text(
                    text = "08:32",
                    color = designSystem.color(ColorToken.TEXT_PRIMARY),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = when(currentSkin) {
                    AnalyzerSkin.STATE_MAP -> designSystem.string(StringToken.SKIN_STATE_MAP)
                    AnalyzerSkin.VOICE_RIBBON -> designSystem.string(StringToken.SKIN_VOICE_RIBBON)
                    AnalyzerSkin.EQUALIZER -> designSystem.string(StringToken.SKIN_EQUALIZER)
                    AnalyzerSkin.RINGS -> designSystem.string(StringToken.SKIN_RINGS)
                },
                color = designSystem.color(ColorToken.TEXT_SECONDARY),
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(Modifier.weight(1f))

        // 2. Main Visualization Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp),
            contentAlignment = Alignment.Center
        ) {
             when(currentSkin) {
                AnalyzerSkin.STATE_MAP -> {
                    StateMapVisualization(jitter, pitch, rms)
                }
                AnalyzerSkin.VOICE_RIBBON -> {
                    VoiceRibbonVisualization(jitter, pitch, rms)
                }
                else -> {
                    Text("Скоро будет доступно", color = designSystem.color(ColorToken.TEXT_SECONDARY))
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // 3. Metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MetricItem("Jitter", jitter.toInt().toString(), Color(0xFF00FFCC))
            MetricItem("Pitch", pitch.toInt().toString(), Color(0xFF3399FF))
            MetricItem("RMS", rms.toInt().toString(), Color(0xFFFFCC33))
        }

        Spacer(Modifier.height(designSystem.dimen(DimenToken.SPACING_LARGE)))

        // 4. Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { 
                    val entries = AnalyzerSkin.entries
                    val nextIndex = (currentSkin.ordinal + 1) % entries.size
                    currentSkin = entries[nextIndex]
                },
                modifier = Modifier.background(designSystem.color(ColorToken.SURFACE_VARIANT), CircleShape)
            ) {
                Icon(
                    imageVector = designSystem.icon(IconToken.MENU), 
                    contentDescription = null, 
                    tint = designSystem.color(ColorToken.TEXT_PRIMARY)
                )
            }

            Spacer(Modifier.width(designSystem.dimen(DimenToken.SPACING_LARGE)))

            FloatingActionButton(
                onClick = { isPaused = !isPaused },
                shape = CircleShape,
                containerColor = designSystem.color(ColorToken.TEXT_PRIMARY),
                contentColor = designSystem.color(ColorToken.TEXT_INVERTED),
                modifier = Modifier.size(designSystem.dimen(DimenToken.BUTTON_HEIGHT) + 16.dp)
            ) {
                Icon(
                    imageVector = if (isPaused) designSystem.icon(IconToken.PLAY) else designSystem.icon(IconToken.PAUSE),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.width(designSystem.dimen(DimenToken.SPACING_LARGE)))

            IconButton(
                onClick = { /* TODO: Stop/Finish */ },
                modifier = Modifier.background(designSystem.color(ColorToken.SURFACE_VARIANT), CircleShape)
            ) {
                Icon(
                    imageVector = designSystem.icon(IconToken.MIC), 
                    contentDescription = null, 
                    tint = designSystem.color(ColorToken.TEXT_PRIMARY)
                )
            }
        }
    }
}

@Composable
fun StateMapVisualization(jitter: Float, pitch: Float, rms: Float) {
    val designSystem = LocalDesignSystem.current
    Canvas(modifier = Modifier.size(300.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2.5f
        
        // Vertices
        val pStress = Offset(center.x, center.y - radius * 1.2f)
        val pPressure = Offset(center.x - radius * 1.1f, center.y + radius * 0.8f)
        val pFear = Offset(center.x + radius * 1.1f, center.y + radius * 0.8f)
        
        // Background Triangle
        val trianglePath = Path().apply {
            moveTo(pStress.x, pStress.y)
            lineTo(pPressure.x, pPressure.y)
            lineTo(pFear.x, pFear.y)
            close()
        }
        
        drawPath(
            path = trianglePath,
            brush = Brush.radialGradient(
                colors = listOf(designSystem.color(ColorToken.ACCENT_PRIMARY).copy(alpha = 0.1f), Color.Transparent),
                center = center,
                radius = radius * 1.5f
            )
        )
        
        drawPath(
            path = trianglePath,
            color = designSystem.color(ColorToken.TEXT_PRIMARY).copy(alpha = 0.2f),
            style = Stroke(width = designSystem.dimen(DimenToken.DIVIDER_THICKNESS).toPx())
        )
        
        // Balance Dot
        val total = jitter + pitch + rms
        val dotX = (jitter * pFear.x + pitch * pStress.x + rms * pPressure.x) / total
        val dotY = (jitter * pFear.y + pitch * pStress.y + rms * pPressure.y) / total
        
        drawCircle(
            color = designSystem.color(ColorToken.TEXT_PRIMARY),
            radius = 6.dp.toPx(),
            center = Offset(dotX, dotY)
        )
        
        drawCircle(
            color = designSystem.color(ColorToken.TEXT_PRIMARY).copy(alpha = 0.3f),
            radius = 12.dp.toPx(),
            center = Offset(dotX, dotY),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

@Composable
fun VoiceRibbonVisualization(jitter: Float, pitch: Float, rms: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "ribbon")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "time"
    )

    Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        val width = size.width
        val height = size.height
        val centerY = height / 2
        
        val colors = listOf(Color(0xFF00FFCC), Color(0xFF3399FF), Color(0xFFFFCC33))
        val factors = listOf(jitter/50f, pitch/100f, rms/30f)
        
        factors.forEachIndexed { index, factor ->
            val path = Path()
            for (x in 0..width.toInt() step 5) {
                val wave = sin((x / width * 10f) + time + (index * 2f)) * (height / 4f) * factor
                if (x == 0) path.moveTo(x.toFloat(), centerY + wave)
                else path.lineTo(x.toFloat(), centerY + wave)
            }
            drawPath(
                path = path,
                color = colors[index].copy(alpha = 0.8f),
                style = Stroke(width = 2.dp.toPx())
            )
        }
        
        // Scanning overlay
        drawRect(
            brush = Brush.horizontalGradient(
                0.8f to Color.Transparent,
                1.0f to Color.White.copy(alpha = 0.1f)
            ),
            size = size
        )
    }
}

@Composable
fun MetricItem(label: String, value: String, color: Color) {
    val designSystem = LocalDesignSystem.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = color, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = label, color = designSystem.color(ColorToken.TEXT_SECONDARY), style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(4.dp))
        Box(modifier = Modifier.width(40.dp).height(2.dp).background(color.copy(alpha = 0.3f)))
    }
}
