package application.poligraf.widgets.state

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import application.poligraf.uicore.theme.LocalDesignSystem
import application.poligraf.uicore.theme.tokens.ColorToken

/**
 * A universal segmented tab row with a "glass" style.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Enum<T>> GlassSegmentedTabRow(
    items: Array<T>,
    selectedIndex: Int,
    onTabSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    labelProvider: @Composable (T) -> String
) {
    val designSystem = LocalDesignSystem.current

    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items.forEachIndexed { index, item ->
            SegmentedButton(
                selected = selectedIndex == index,
                onClick = { onTabSelected(item) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = items.size
                ),
                icon = {}, // Remove the default checkmark
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = designSystem.color(ColorToken.ACCENT_PRIMARY)
                        .copy(alpha = 0.2f),
                    activeContentColor = designSystem.color(ColorToken.ACCENT_PRIMARY),
                    inactiveContainerColor = designSystem.color(ColorToken.GLASS_BASE),
                    inactiveContentColor = designSystem.color(ColorToken.TEXT_SECONDARY),
                    activeBorderColor = designSystem.color(ColorToken.ACCENT_PRIMARY)
                        .copy(alpha = 0.5f),
                    inactiveBorderColor = designSystem.color(ColorToken.GLASS_BORDER)
                )
            ) {
                Text(
                    labelProvider(item),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
