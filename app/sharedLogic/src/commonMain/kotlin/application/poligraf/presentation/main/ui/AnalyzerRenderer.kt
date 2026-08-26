package application.poligraf.presentation.main.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import application.poligraf.presentation.main.AnalyzerViewModel
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.DimenToken
import application.poligraf.ui.theme.tokens.StringToken
import application.poligraf.ui.features.recorder.*
import application.poligraf.ui.foundation.types.AnalyzerSkin

@Composable
fun AnalyzerRenderer(
    viewModel: AnalyzerViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val designSystem = LocalDesignSystem.current
    val state by viewModel.state.collectAsState()
    
    val displayJitter = state.displayFrame?.jitter ?: 0f
    val displayPitch = state.displayFrame?.pitch ?: 0f
    val displayRms = state.displayFrame?.rms ?: 0f
    
    // Determine current anomaly color for background glow
    val anomalyColor = remember(state.displayFrame) {
        val frame = state.displayFrame
        if (frame?.isAnomaly == true) {
            when {
                frame.jitter > 12f -> designSystem.color(ColorToken.CHART_JITTER)
                frame.pitch > 220f -> designSystem.color(ColorToken.CHART_PITCH)
                else -> designSystem.color(ColorToken.CHART_RMS)
            }
        } else Color.Transparent
    }

    // Ambient background pulse spec
    val infiniteTransition = rememberInfiniteTransition(label = "ambient")
    val ambientAlpha by infiniteTransition.animateFloat(
        initialValue = 0.10f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "alpha"
    )

    LaunchedEffect(Unit) {
        viewModel.onAppear()
    }

    val currentSkinName = when(state.currentSkin) {
        AnalyzerSkin.STATE_MAP -> designSystem.string(StringToken.SKIN_STATE_MAP)
        AnalyzerSkin.VOICE_RIBBON -> designSystem.string(StringToken.SKIN_VOICE_RIBBON)
        AnalyzerSkin.EQUALIZER -> designSystem.string(StringToken.SKIN_EQUALIZER)
        AnalyzerSkin.RINGS -> designSystem.string(StringToken.SKIN_RINGS)
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(designSystem.color(ColorToken.SURFACE_BACKGROUND))) {
        val width = constraints.maxWidth.toFloat()

        // 0. Ambient Background Glow (Gemini-style)
        AnimatedVisibility(
            visible = state.isDisplayAnomalous,
            enter = fadeIn(tween(1200, easing = LinearOutSlowInEasing)),
            exit = fadeOut(tween(1500, easing = LinearOutSlowInEasing))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                anomalyColor.copy(alpha = ambientAlpha * 1.4f),
                                anomalyColor.copy(alpha = ambientAlpha * 0.6f),
                                Color.Transparent
                            ),
                            center = Offset(width / 2f, 0f), 
                            radius = 1800f
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(designSystem.dimen(DimenToken.SPACING_LARGE)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Header
            AnalyzerHeader(
                durationText = state.durationText,
                isRecording = state.isRecording,
                isPaused = state.isPaused,
                onSave = {
                    viewModel.onStop(save = true)
                    onClose()
                },
                onDelete = {
                    viewModel.onStop(save = false)
                    onClose()
                }
            )

            // 2. Global Status Overlay (Above Visualization)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = state.activeInterpretation?.let { "interp" } ?: "none",
                    transitionSpec = {
                        fadeIn(tween(600)) togetherWith fadeOut(tween(600))
                    }, label = "status"
                ) { target ->
                    when(target) {
                        "interp" -> {
                            val rawText = state.activeInterpretation?.let { designSystem.string(it) } ?: ""
                            val format = designSystem.string(StringToken.INTERPRETATION_FORMAT)
                            val finalTitle = format.replace("%s", rawText)

                            Text(
                                text = finalTitle,
                                color = designSystem.color(ColorToken.TEXT_PRIMARY),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Light,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                ),
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                        else -> {
                            // Empty space to maintain layout height
                            Spacer(Modifier.height(80.dp))
                        }
                    }
                }
            }

            // 3. Main Visualization Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                // Visualization Skin
                Box {
                    when(state.currentSkin) {
                        AnalyzerSkin.STATE_MAP -> StateMapVisualization(
                            jitterLevel = state.jitterLevel,
                            pitchLevel = state.pitchLevel,
                            rmsLevel = state.rmsLevel
                        )
                        AnalyzerSkin.VOICE_RIBBON -> VoiceRibbonVisualization(
                            jitterLevel = state.jitterLevel,
                            pitchLevel = state.pitchLevel,
                            rmsLevel = state.rmsLevel,
                            isPaused = state.isPaused
                        )
                        AnalyzerSkin.EQUALIZER -> EqualizerVisualization(
                            jitterLevel = state.jitterLevel,
                            pitchLevel = state.pitchLevel,
                            rmsLevel = state.rmsLevel
                        )
                        AnalyzerSkin.RINGS -> RingsVisualization(
                            jitterLevel = state.jitterLevel,
                            pitchLevel = state.pitchLevel,
                            rmsLevel = state.rmsLevel
                        )
                    }
                }
            }

            // 3. Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricItem(
                    label = designSystem.string(StringToken.METRIC_JITTER), 
                    value = if (displayJitter > 0f) "${displayJitter.toInt()}%" else "0%", 
                    color = designSystem.color(ColorToken.CHART_JITTER)
                )
                MetricItem(
                    label = designSystem.string(StringToken.METRIC_PITCH), 
                    value = if (displayPitch > 50f) displayPitch.toInt().toString() else "0", 
                    color = designSystem.color(ColorToken.CHART_PITCH)
                )
                MetricItem(
                    label = designSystem.string(StringToken.METRIC_RMS), 
                    value = (displayRms * 100).toInt().coerceIn(0, 100).toString(), 
                    color = designSystem.color(ColorToken.CHART_RMS)
                )
            }

            Spacer(Modifier.height(designSystem.dimen(DimenToken.SPACING_LARGE)))
            
            // 3a. Anomaly Timeline
            AnomalyTimeline(
                markers = state.timelineMarkers,
                currentDurationMillis = state.currentDurationMillis,
                seekPositionMillis = state.seekPositionMillis,
                isPaused = state.isPaused,
                onSeek = viewModel::onSeek
            )

            Spacer(Modifier.height(designSystem.dimen(DimenToken.SPACING_LARGE)))

            // 4. Controls
            AnalyzerControls(
                isRecording = state.isRecording,
                isPaused = state.isPaused,
                currentSkin = state.currentSkin,
                currentSkinName = currentSkinName,
                onStart = viewModel::onStart,
                onPauseResume = viewModel::onPauseResume,
                onSkinChange = {
                    val entries = AnalyzerSkin.entries
                    val nextIndex = (state.currentSkin.ordinal + 1) % entries.size
                    viewModel.onSkinChange(entries[nextIndex])
                }
            )

            Spacer(Modifier.height(designSystem.dimen(DimenToken.SPACING_LARGE)))
        }
    }
}
