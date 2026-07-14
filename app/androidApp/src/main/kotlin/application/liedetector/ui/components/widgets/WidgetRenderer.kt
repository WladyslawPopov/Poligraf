package application.liedetector.ui.components.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import application.liedetector.uicore.theme.LocalDesignSystem
import application.liedetector.uiwidgets.models.WidgetAction
import application.liedetector.uiwidgets.models.WidgetDto

@Composable
fun WidgetRenderer(
    widget: WidgetDto,
    onAction: (WidgetAction) -> Unit
) {
    val designSystem = LocalDesignSystem.current
    
    when (widget) {
        is WidgetDto.Header -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = designSystem.string(widget.titleKey),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                widget.subtitleKey?.let {
                    Text(
                        text = designSystem.string(it),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = designSystem.icon("mic"),
                        contentDescription = "Record"
                    )
                }
            }
        }
        
        is WidgetDto.StandardButton -> {
            Button(
                onClick = { onAction(widget.action) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                shape = MaterialTheme.shapes.large,
                colors = if (widget.isPrimary) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
            ) {
                Text(text = designSystem.string(widget.textKey))
            }
        }
        
        else -> {
            // Placeholder for unknown widgets
            Text("Unknown widget: ${widget::class.simpleName}")
        }
    }
}
