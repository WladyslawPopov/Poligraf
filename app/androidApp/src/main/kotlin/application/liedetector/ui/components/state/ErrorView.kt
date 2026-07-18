package application.liedetector.ui.components.state

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import application.liedetector.theme.utils.composeColor
import application.liedetector.uicore.state.ErrorType
import application.liedetector.uicore.theme.*

/**
 * Renders an error state. 
 * Now uses Dialog for all types to be non-intrusive to the underlying screen structure.
 */
@Composable
fun ErrorView(
    type: ErrorType,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    val designSystem = LocalDesignSystem.current

    val titleToken = when (type) {
        ErrorType.NO_INTERNET -> StringToken.ERROR_NO_INTERNET_TITLE
        ErrorType.SERVER_UNAVAILABLE -> StringToken.ERROR_SERVER_TITLE
        else -> StringToken.ERROR_UNKNOWN_TITLE
    }

    val msgToken = when (type) {
        ErrorType.NO_INTERNET -> StringToken.ERROR_NO_INTERNET_MSG
        ErrorType.SERVER_UNAVAILABLE -> StringToken.ERROR_SERVER_MSG
        else -> StringToken.ERROR_UNKNOWN_MSG
    }

    if (type == ErrorType.UNKNOWN || type == ErrorType.UNAUTHORIZED) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(designSystem.string(titleToken)) },
            text = { Text(designSystem.string(msgToken)) },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(designSystem.string(StringToken.ERROR_RETRY))
                }
            },
            containerColor = designSystem.composeColor(ColorToken.SURFACE),
            titleContentColor = designSystem.composeColor(ColorToken.TEXT_PRIMARY),
            textContentColor = designSystem.composeColor(ColorToken.TEXT_SECONDARY)
        )
    } else {
        // Full screen overlay using Dialog so it doesn't break the Scaffold body composition
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(designSystem.composeColor(ColorToken.BACKGROUND)) // Keep it opaque to focus on the error
                    .padding(designSystem.dimen(DimenToken.PADDING_ERROR).dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = designSystem.string(titleToken),
                        style = MaterialTheme.typography.headlineMedium,
                        color = designSystem.composeColor(ColorToken.TEXT_PRIMARY),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(designSystem.dimen(DimenToken.SPACING_SMALL).dp))
                    Text(
                        text = designSystem.string(msgToken),
                        style = MaterialTheme.typography.bodyLarge,
                        color = designSystem.composeColor(ColorToken.TEXT_SECONDARY),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(designSystem.dimen(DimenToken.SPACING_LARGE).dp))
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = designSystem.composeColor(ColorToken.PRIMARY),
                            contentColor = designSystem.composeColor(ColorToken.ON_PRIMARY)
                        )
                    ) {
                        Text(designSystem.string(StringToken.ERROR_RETRY))
                    }
                }
            }
        }
    }
}
