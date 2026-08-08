package application.liedetector.ui.components.state

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import application.liedetector.theme.utils.composeColor
import application.liedetector.uicore.theme.DesignSystem
import application.liedetector.uicore.theme.tokens.ColorToken

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    designSystem: DesignSystem,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = designSystem.composeColor(ColorToken.SURFACE).copy(alpha = 0.95f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = designSystem.composeColor(ColorToken.TEXT_SECONDARY).copy(alpha = 0.3f)
            )
        },
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = designSystem.composeColor(ColorToken.TEXT_PRIMARY),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
                
                // Simple clean divider
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 1.dp,
                    color = designSystem.composeColor(ColorToken.GLASS_BORDER).copy(alpha = 0.1f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }

            content()
        }
    }
}
