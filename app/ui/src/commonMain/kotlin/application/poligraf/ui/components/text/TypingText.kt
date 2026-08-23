package application.poligraf.ui.components.text

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/**
 * A reusable component that types out text character by character.
 */
@Composable
fun TypingText(
    fullText: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    typingDelay: Long = 40L,
    onComplete: () -> Unit = {}
) {
    var displayedText by remember(fullText) { mutableStateOf("") }

    LaunchedEffect(fullText) {
        displayedText = ""
        fullText.forEach { char ->
            displayedText += char
            delay(typingDelay.milliseconds)
        }
        onComplete()
    }

    Box(modifier = modifier) {
        // Ghost Layer to reserve space
        Text(
            text = fullText,
            style = style,
            color = Color.Transparent,
            textAlign = textAlign
        )

        // Visible Layer
        Text(
            text = displayedText,
            style = style,
            color = color,
            textAlign = textAlign
        )
    }
}
