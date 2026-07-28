package application.liedetector.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import application.liedetector.theme.utils.composeColor
import application.liedetector.uicore.theme.ColorToken
import application.liedetector.uicore.theme.LocalDesignSystem

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
    labelProvider: (T) -> String
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
                    activeContainerColor = designSystem.composeColor(ColorToken.ACCENT_PRIMARY)
                        .copy(alpha = 0.2f),
                    activeContentColor = designSystem.composeColor(ColorToken.ACCENT_PRIMARY),
                    inactiveContainerColor = designSystem.composeColor(ColorToken.GLASS_BASE),
                    inactiveContentColor = designSystem.composeColor(ColorToken.TEXT_SECONDARY),
                    activeBorderColor = designSystem.composeColor(ColorToken.ACCENT_PRIMARY)
                        .copy(alpha = 0.5f),
                    inactiveBorderColor = designSystem.composeColor(ColorToken.GLASS_BORDER)
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
