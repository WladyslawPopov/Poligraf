package application.poligraf.ui.features.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import application.poligraf.domain.model.MarkerShape
import application.poligraf.ui.components.buttons.AppIconButton
import application.poligraf.ui.features.recorder.drawMarker
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(designSystem.color(ColorToken.SURFACE_PRIMARY), MaterialTheme.shapes.medium)
            .padding(designSystem.dimen(DimenToken.SPACING_MEDIUM))
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
                if (markerColor != null && markerShape != null) {
                    Canvas(modifier = Modifier.size(12.dp)) {
                        drawMarker(
                            shape = markerShape,
                            color = designSystem.color(markerColor),
                            center = Offset(size.width / 2, size.height / 2),
                            size = size.minDimension
                        )
                    }
                }
                
                Text(
                    text = timestampText,
                    color = designSystem.color(ColorToken.ACCENT_PRIMARY),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            AppIconButton(
                icon = IconToken.DELETE,
                tint = ColorToken.STATE_ERROR,
                onClick = onDelete
            )
        }
        
        Spacer(Modifier.height(4.dp))
        
        Text(
            text = text,
            color = designSystem.color(ColorToken.TEXT_PRIMARY),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
