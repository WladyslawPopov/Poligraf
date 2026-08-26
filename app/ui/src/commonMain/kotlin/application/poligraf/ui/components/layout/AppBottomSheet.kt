package application.poligraf.ui.components.layout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import application.poligraf.ui.theme.DesignSystem
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.DimenToken

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomSheet(
    sheetState: SheetState,
    designSystem: DesignSystem,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        dragHandle = null,
        scrimColor = Color.Black.copy(alpha = 0.4f),
    ) {
        Surface(
            modifier = modifier.fillMaxHeight(),
            color = designSystem.color(ColorToken.SURFACE_PRIMARY).copy(alpha = 0.92f),
            shape = MaterialTheme.shapes.extraLarge,
            border = BorderStroke(
                designSystem.dimen(DimenToken.DIVIDER_THICKNESS),
                Brush.linearGradient(
                    listOf(
                        designSystem.color(ColorToken.ACCENT_PRIMARY).copy(alpha = 0.1f),
                        designSystem.color(ColorToken.ACCENT_PRIMARY).copy(alpha = 0.4f),
                        designSystem.color(ColorToken.ACCENT_PRIMARY).copy(alpha = 0.1f),
                    )
                )
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Subtle Drag Handle
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .size(width = 36.dp, height = 4.dp)
                        .background(
                            color = designSystem.color(ColorToken.TEXT_SECONDARY).copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                        .align(Alignment.CenterHorizontally)
                )
                
                content()
            }
        }
    }
}
