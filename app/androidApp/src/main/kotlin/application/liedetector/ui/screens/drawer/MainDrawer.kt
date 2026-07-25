package application.liedetector.ui.screens.drawer

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import application.liedetector.theme.ThemeState
import application.liedetector.theme.utils.composeColor
import application.liedetector.uicore.theme.ColorToken
import application.liedetector.uicore.theme.DesignSystem
import application.liedetector.uicore.theme.DimenToken
import application.liedetector.uicore.theme.StringToken
import androidx.compose.foundation.clickable
import application.liedetector.navigation.AppNavigation

@Composable
fun MainDrawer(
    designSystem: DesignSystem,
    navigation: AppNavigation
){
    ModalDrawerSheet(
        drawerContainerColor = designSystem.composeColor(ColorToken.SURFACE),
        drawerShape = RoundedCornerShape(
            topEnd = designSystem.dimen(DimenToken.DRAWER_CORNER).dp,
            bottomEnd = designSystem.dimen(DimenToken.DRAWER_CORNER).dp
        )
    )
    {
        Spacer(modifier = Modifier.height(designSystem.dimen(DimenToken.SPACING_LARGE).dp))
        Text(
            text = designSystem.string(StringToken.DRAWER_SETTINGS),
            modifier = Modifier.padding(designSystem.dimen(DimenToken.SPACING_MEDIUM).dp),
            style = MaterialTheme.typography.headlineSmall,
            color = designSystem.composeColor(ColorToken.TEXT_PRIMARY)
        )
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = designSystem.dimen(DimenToken.SPACING_MEDIUM).dp)
        )

        val isDark by ThemeState.isDark.collectAsState()

        ListItem(
            headlineContent = {
                Text(
                    designSystem.string(StringToken.DRAWER_DARK_MODE),
                    color = designSystem.composeColor(ColorToken.TEXT_PRIMARY)
                )
            },
            trailingContent = {
                Switch(
                    checked = isDark,
                    onCheckedChange = { ThemeState.toggle() }
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )

        if (designSystem.isDebug) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))
            ListItem(
                headlineContent = {
                    Text(
                        designSystem.string(StringToken.OPEN_DEBUG_SANDBOX),
                        color = designSystem.composeColor(ColorToken.ACCENT_ENERGY)
                    )
                },
                modifier = Modifier.clickable {
                    navigation.setDrawerOpen(false)
                    navigation.openDebug()
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
        }
    }
}
