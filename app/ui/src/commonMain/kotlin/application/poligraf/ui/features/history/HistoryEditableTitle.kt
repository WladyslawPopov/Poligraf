package application.poligraf.ui.features.history

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken

@Composable
fun HistoryEditableTitle(
    title: String,
    onTitleChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val designSystem = LocalDesignSystem.current

    TextField(
        value = title,
        onValueChange = onTitleChange,
        modifier = modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedTextColor = designSystem.color(ColorToken.TEXT_PRIMARY),
            unfocusedTextColor = designSystem.color(ColorToken.TEXT_PRIMARY),
            cursorColor = designSystem.color(ColorToken.ACCENT_PRIMARY),
            focusedIndicatorColor = designSystem.color(ColorToken.ACCENT_PRIMARY)
        ),
        textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
        singleLine = true
    )
}
