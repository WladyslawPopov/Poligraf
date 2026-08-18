package application.poligraf.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import application.poligraf.uicore.theme.LocalDesignSystem
import application.poligraf.uicore.theme.tokens.DimenToken
import application.poligraf.uicore.widgets.UiWidget
import application.poligraf.widgets.utils.composeColor
import application.poligraf.widgets.utils.typography
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
    style: TextStyle = MaterialTheme.typography.headlineLarge,
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
