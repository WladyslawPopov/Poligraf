package application.liedetector.ui.components.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import application.liedetector.uicore.theme.LocalDesignSystem
import application.liedetector.uiwidgets.models.WidgetAction
import application.liedetector.uiwidgets.models.UiWidget
import application.liedetector.theme.utils.composeColor
import application.liedetector.uicore.theme.ColorToken
import application.liedetector.uicore.theme.DimenToken

@Composable
fun WidgetRenderer(
    widget: UiWidget,
    onAction: (WidgetAction) -> Unit
) {
    val designSystem = LocalDesignSystem.current
    
    when (widget) {
        is UiWidget.Header -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = designSystem.dimen(DimenToken.SPACING_MEDIUM).dp, 
                        vertical = designSystem.dimen(DimenToken.SPACING_SMALL).dp
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = designSystem.composeColor(ColorToken.SURFACE_VARIANT)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(designSystem.dimen(DimenToken.SPACING_LARGE).dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = designSystem.string(widget.titleKey),
                        style = MaterialTheme.typography.headlineMedium,
                        color = designSystem.composeColor(ColorToken.TEXT_PRIMARY),
                        textAlign = TextAlign.Center
                    )
                    widget.subtitleKey?.let {
                        Spacer(modifier = Modifier.height(designSystem.dimen(DimenToken.SPACING_TINY).dp))
                        Text(
                            text = designSystem.string(it),
                            style = MaterialTheme.typography.bodyMedium,
                            color = designSystem.composeColor(ColorToken.TEXT_SECONDARY),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        
        is UiWidget.MicrophoneButton -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(designSystem.dimen(DimenToken.SPACING_LARGE).dp),
                contentAlignment = Alignment.Center
            ) {
                LargeFloatingActionButton(
                    onClick = { onAction(widget.action) },
                    containerColor = designSystem.composeColor(ColorToken.PRIMARY),
                    contentColor = designSystem.composeColor(ColorToken.ON_PRIMARY),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = designSystem.icon("mic"),
                        contentDescription = "Record",
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
        
        is UiWidget.StandardButton -> {
            Button(
                onClick = { onAction(widget.action) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = designSystem.dimen(DimenToken.SPACING_MEDIUM).dp, 
                        vertical = designSystem.dimen(DimenToken.SPACING_SMALL).dp
                    )
                    .height(designSystem.dimen(DimenToken.BUTTON_HEIGHT).dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = designSystem.composeColor(ColorToken.SURFACE_VARIANT),
                    contentColor = designSystem.composeColor(ColorToken.TEXT_PRIMARY)
                )
            ) {
                Text(
                    text = designSystem.string(widget.textKey),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        
        else -> {
            Text(
                text = "Unknown: ${widget::class.simpleName}", 
                color = designSystem.composeColor(ColorToken.TEXT_SECONDARY)
            )
        }
    }
}
