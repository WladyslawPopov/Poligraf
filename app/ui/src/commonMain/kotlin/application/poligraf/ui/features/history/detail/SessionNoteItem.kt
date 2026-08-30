package application.poligraf.ui.features.history.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import application.poligraf.domain.model.MarkerShape
import application.poligraf.ui.components.buttons.AppIconButton
import application.poligraf.ui.components.containers.AppCard
import application.poligraf.ui.features.analyzer.components.drawMarker
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.DimenToken
import application.poligraf.ui.theme.tokens.IconToken

@Composable
fun SessionNoteItem(
    timestampText: String,
    text: String,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    markerColor: ColorToken? = null,
    markerShape: MarkerShape? = null
) {
    val designSystem = LocalDesignSystem.current
    val painter = markerShape?.let { shape ->
        val iconToken = when (shape) {
            MarkerShape.CIRCLE -> IconToken.SHAPE_CIRCLE
            MarkerShape.STAR -> IconToken.SHAPE_STAR
            MarkerShape.DIAMOND -> IconToken.SHAPE_DIAMOND
            MarkerShape.HEART -> IconToken.SHAPE_HEART
        }
        rememberVectorPainter(designSystem.icon(iconToken))
    }

    AppCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Vertical Indicator for Anomaly type
            if (markerColor != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(designSystem.color(markerColor).copy(alpha = 0.6f))
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = if (markerColor != null) 16.dp else designSystem.dimen(DimenToken.SPACING_MEDIUM),
                        end = designSystem.dimen(DimenToken.SPACING_MEDIUM),
                        top = designSystem.dimen(DimenToken.SPACING_SMALL),
                        bottom = designSystem.dimen(DimenToken.SPACING_SMALL)
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (markerColor != null && painter != null) {
                            Canvas(modifier = Modifier.size(14.dp)) {
                                drawMarker(
                                    painter = painter,
                                    color = designSystem.color(markerColor),
                                    center = Offset(size.width / 2, size.height / 2),
                                    size = size.minDimension
                                )
                            }
                        }

                        Text(
                            text = timestampText,
                            color = designSystem.color(ColorToken.TEXT_SECONDARY).copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    AppIconButton(
                        icon = IconToken.DELETE,
                        tint = ColorToken.STATE_ERROR,
                        onClick = onDelete
                    )
                }

                Spacer(Modifier.height(2.dp))

                Text(
                    text = text,
                    color = designSystem.color(ColorToken.TEXT_PRIMARY),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight.times(1.2f)
                )
            }
        }
    }
}
