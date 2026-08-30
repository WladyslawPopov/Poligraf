package application.poligraf.ui.features.history.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import application.poligraf.ui.components.buttons.AppIconButton
import application.poligraf.ui.components.text.SectionHeader
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.IconToken
import application.poligraf.ui.theme.tokens.StringToken

@Composable
fun HistoryNotesField(
    notes: String,
    onNotesChange: (String) -> Unit,
    onAddNote: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val designSystem = LocalDesignSystem.current

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        SectionHeader(titleToken = StringToken.LABEL_NOTES)

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = notes,
                onValueChange = onNotesChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = designSystem.string(StringToken.NOTES_HINT),
                        style = MaterialTheme.typography.bodyMedium,
                        color = designSystem.color(ColorToken.TEXT_SECONDARY).copy(alpha = 0.5f)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = designSystem.color(ColorToken.SURFACE_PRIMARY),
                    unfocusedContainerColor = designSystem.color(ColorToken.SURFACE_PRIMARY),
                    focusedTextColor = designSystem.color(ColorToken.TEXT_PRIMARY),
                    unfocusedTextColor = designSystem.color(ColorToken.TEXT_PRIMARY),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                shape = MaterialTheme.shapes.large,
                textStyle = MaterialTheme.typography.bodyMedium,
                maxLines = 3
            )

            AppIconButton(
                icon = IconToken.NOTE,
                tint = ColorToken.ACCENT_PRIMARY,
                onClick = onAddNote
            )
        }
    }
}
