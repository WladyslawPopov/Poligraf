package application.poligraf.ui.features.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken
import androidx.compose.ui.platform.LocalFocusManager
import application.poligraf.ui.theme.tokens.DimenToken
import application.poligraf.ui.theme.tokens.IconToken

@Composable
fun HistoryEditableTitle(
    title: String,
    isEditing: Boolean,
    onTitleChange: (String) -> Unit,
    onToggleEdit: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val designSystem = LocalDesignSystem.current
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = title, selection = TextRange(title.length)))
    }

    // Update local state when title from props changes (e.g. from outside)
    LaunchedEffect(title) {
        if (textFieldValue.text != title) {
            textFieldValue = textFieldValue.copy(text = title)
        }
    }

    // Set cursor to end when editing starts
    LaunchedEffect(isEditing) {
        if (isEditing) {
            textFieldValue = textFieldValue.copy(
                selection = TextRange(textFieldValue.text.length)
            )
            focusRequester.requestFocus()
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = designSystem.dimen(DimenToken.SPACING_MEDIUM)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clickable { onToggleEdit(true) },
            contentAlignment = Alignment.CenterStart
        ) {
            if (isEditing) {
                TextField(
                    value = textFieldValue,
                    onValueChange = {
                        textFieldValue = it
                        onTitleChange(it.text)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = designSystem.color(ColorToken.TEXT_PRIMARY),
                        unfocusedTextColor = designSystem.color(ColorToken.TEXT_PRIMARY),
                        cursorColor = designSystem.color(ColorToken.ACCENT_PRIMARY),
                        focusedIndicatorColor = designSystem.color(ColorToken.ACCENT_PRIMARY),
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    singleLine = true
                )
            } else {
                Text(
                    text = title.ifEmpty { "Session Name" },
                    color = designSystem.color(ColorToken.TEXT_PRIMARY),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        IconButton(
            onClick = { 
                if (isEditing) focusManager.clearFocus()
                onToggleEdit(!isEditing) 
            },
            modifier = Modifier
                .padding(start = 12.dp)
                .size(44.dp)
                .background(
                    designSystem.color(ColorToken.GLASS_BASE).copy(alpha = 0.2f),
                    MaterialTheme.shapes.medium
                )
        ) {
            Icon(
                imageVector = designSystem.icon(if (isEditing) IconToken.CHECK else IconToken.EDIT),
                contentDescription = null,
                tint = designSystem.color(ColorToken.ACCENT_PRIMARY),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
