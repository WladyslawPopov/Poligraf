package application.liedetector.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import application.liedetector.uicore.theme.tokens.DimenToken
import application.liedetector.uicore.theme.LocalDesignSystem
import application.liedetector.uicore.widgets.UiWidget
import application.liedetector.widgets.utils.composeColor
import application.liedetector.widgets.utils.typography
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun WelcomeTextRenderer(
    widget: UiWidget.WelcomeText
) {
    val designSystem = LocalDesignSystem.current
    val fullText = designSystem.string(widget.textToken) + (widget.emoji ?: "")
    
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val isLandscape = maxWidth > maxHeight
        
        val minHeight = if (isLandscape) {
            designSystem.dimen(DimenToken.WELCOME_MIN_HEIGHT).dp / 2.5f
        } else {
            designSystem.dimen(DimenToken.WELCOME_MIN_HEIGHT).dp
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(designSystem.dimen(DimenToken.SPACING_LARGE).dp)
                .heightIn(min = minHeight),
            verticalArrangement = Arrangement.Center
        ) {
            TypingText(
                text = fullText,
                style = designSystem.typography(widget.typographyToken),
                color = designSystem.composeColor(widget.colorToken),
                typingDelay = widget.typingDelay
            )
        }
    }
}

@Composable
fun TypingText(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.headlineLarge,
    color: Color = Color.Unspecified,
    typingDelay: Long = 40L
) {
    var displayedText by remember { mutableStateOf("") }
    
    LaunchedEffect(text) {
        displayedText = ""
        // Simple character-based typing for CMP commonMain
        text.forEach { char ->
            displayedText += char
            delay(typingDelay.milliseconds)
        }
    }
    
    Box(modifier = modifier) {
        // Ghost Layer
        Text(
            text = text,
            style = style,
            color = Color.Transparent
        )
        
        // Animated Layer
        Text(
            text = displayedText,
            style = style,
            color = color
        )
    }
}
