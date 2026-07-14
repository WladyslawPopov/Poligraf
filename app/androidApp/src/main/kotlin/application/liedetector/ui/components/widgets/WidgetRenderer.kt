package application.liedetector.ui.components.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import application.liedetector.uicore.theme.LocalDesignSystem
import application.liedetector.uiwidgets.models.WidgetAction
import application.liedetector.uiwidgets.models.WidgetDto
import application.liedetector.theme.utils.glassPanel

@Composable
fun WidgetRenderer(
    widget: WidgetDto,
    onAction: (WidgetAction) -> Unit
) {
    val designSystem = LocalDesignSystem.current
    
    when (widget) {
        is WidgetDto.Header -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .glassPanel(designSystem)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = designSystem.string(widget.titleKey),
                        style = MaterialTheme.typography.headlineMedium,
                        color = androidx.compose.ui.graphics.Color.White,
                        textAlign = TextAlign.Center
                    )
                    widget.subtitleKey?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = designSystem.string(it),
                            style = MaterialTheme.typography.bodyMedium,
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        
        is WidgetDto.MicrophoneButton -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                FloatingActionButton(
                    onClick = { onAction(widget.action) },
                    containerColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f),
                    contentColor = androidx.compose.ui.graphics.Color.White,
                    shape = androidx.compose.foundation.shape.CircleShape
                ) {
                    Icon(
                        imageVector = designSystem.icon("mic"),
                        contentDescription = "Record",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
        
        is WidgetDto.StandardButton -> {
            Button(
                onClick = { onAction(widget.action) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .height(56.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.1f),
                    contentColor = androidx.compose.ui.graphics.Color.White
                ),
                border = androidx.compose.foundation.BorderStroke(
                    0.5.dp, 
                    androidx.compose.ui.graphics.Color.White.copy(alpha = 0.25f)
                )
            ) {
                Text(
                    text = designSystem.string(widget.textKey),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        
        else -> {
            Text("Unknown: ${widget::class.simpleName}", color = androidx.compose.ui.graphics.Color.Gray)
        }
    }
}
