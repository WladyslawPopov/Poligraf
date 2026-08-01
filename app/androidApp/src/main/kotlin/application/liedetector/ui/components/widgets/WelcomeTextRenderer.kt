package application.liedetector.ui.components.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.compose.ui.unit.dp
import application.liedetector.uicore.theme.tokens.DimenToken
import application.liedetector.uicore.theme.LocalDesignSystem
import application.liedetector.uicore.widgets.UiWidget
import application.liedetector.theme.utils.composeColor
import application.liedetector.theme.utils.typography
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun WelcomeTextRenderer(
    widget: UiWidget.WelcomeText
) {
    val designSystem = LocalDesignSystem.current
    val fullText = designSystem.string(widget.textToken) + (widget.emoji ?: "")
    
    // We get orientation info from the common logic now if needed, 
    // but the ViewModel can already adjust the widget properties.
    // For now, let's keep the minHeight logic here but simplified.
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
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
        val codePoints = mutableListOf<String>()
        var i = 0
        while (i < text.length) {
            val codePoint = text.codePointAt(i)
            val charCount = Character.charCount(codePoint)
            codePoints.add(text.substring(i, i + charCount))
            i += charCount
        }
        
        codePoints.forEach { symbol ->
            displayedText += symbol
            delay(typingDelay.milliseconds)
        }
    }
    
    // Ghost Text Technique: Use Box to overlay animated text on top of invisible full text
    Box(modifier = modifier) {
        // 1. Ghost Layer: Invisible but reserves full space
        Text(
            text = text,
            style = style,
            color = Color.Transparent // Reserves space without being visible
        )
        
        // 2. Animated Layer: Shows characters as they are "typed"
        Text(
            text = displayedText,
            style = style,
            color = color
        )
    }
}
