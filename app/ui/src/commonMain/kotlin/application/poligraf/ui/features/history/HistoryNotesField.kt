package application.poligraf.ui.features.history

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.DimenToken
import application.poligraf.ui.theme.tokens.StringToken

@Composable
fun HistoryNotesField(
    notes: String,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean,
    modifier: Modifier = Modifier
) {
    val designSystem = LocalDesignSystem.current

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = designSystem.string(StringToken.LABEL_NOTES),
            color = designSystem.color(ColorToken.TEXT_SECONDARY),
            style = MaterialTheme.typography.labelLarge
        )

        TextField(
            value = notes,
            onValueChange = onNotesChange,
            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
            placeholder = { Text(designSystem.string(StringToken.NOTES_HINT)) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = designSystem.color(ColorToken.SURFACE_PRIMARY),
                unfocusedContainerColor = designSystem.color(ColorToken.SURFACE_PRIMARY),
                focusedTextColor = designSystem.color(ColorToken.TEXT_PRIMARY),
                unfocusedTextColor = designSystem.color(ColorToken.TEXT_PRIMARY)
            )
        )

        Spacer(Modifier.height(designSystem.dimen(DimenToken.SPACING_MEDIUM)))

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = designSystem.color(ColorToken.ACCENT_PRIMARY)),
            enabled = !isSaving
        ) {
            Text(text = designSystem.string(StringToken.SAVE))
        }
    }
}
