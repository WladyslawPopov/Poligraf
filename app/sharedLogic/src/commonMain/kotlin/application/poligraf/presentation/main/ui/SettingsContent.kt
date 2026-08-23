package application.poligraf.presentation.main.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import application.poligraf.engine.theme.ThemeManager
import application.poligraf.ui.theme.DesignSystem
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.DimenToken
import application.poligraf.ui.theme.tokens.StringToken
import application.poligraf.ui.components.decorators.GlassDivider
import application.poligraf.ui.components.items.DrawerItem
import org.koin.compose.koinInject

@Composable
fun SettingsContent(
    appVersion: String,
    designSystem: DesignSystem,
    onDebugClicked: () -> Unit,
    themeManager: ThemeManager = koinInject()
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(designSystem.dimen(DimenToken.SPACING_LARGE)),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(designSystem.dimen(DimenToken.SPACING_MEDIUM))
    ) {
        // Header Area
        Text(
            text = designSystem.string(StringToken.SETTINGS),
            style = MaterialTheme.typography.headlineMedium,
            color = designSystem.color(ColorToken.TEXT_PRIMARY),
            fontWeight = FontWeight.ExtraBold
        )

        GlassDivider(designSystem)

        // Settings Content
        DrawerItem(
            label = designSystem.string(StringToken.DARK_MODE),
            designSystem = designSystem,
            trailing = {
                Switch(
                    checked = designSystem.isDark,
                    onCheckedChange = { themeManager.toggleTheme() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = designSystem.color(ColorToken.ACCENT_PRIMARY),
                        checkedTrackColor = designSystem.color(ColorToken.ACCENT_PRIMARY).copy(alpha = 0.3f),
                        uncheckedBorderColor = designSystem.color(ColorToken.TEXT_SECONDARY).copy(alpha = 0.5f)
                    )
                )
            }
        )

        if (designSystem.isDebug) {
            GlassDivider(designSystem)

            DrawerItem(
                label = designSystem.string(StringToken.DEBUG_TITLE),
                designSystem = designSystem,
                color = designSystem.color(ColorToken.ACCENT_ENERGY),
                onClick = onDebugClicked
            )
        }

        // Footer Info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.End),
        ) {
            Text(
                text = designSystem.string(StringToken.FOOTER_TITLE).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = designSystem.color(ColorToken.TEXT_PRIMARY),
                fontWeight = FontWeight.Bold,
                letterSpacing = TextUnit.Unspecified
            )
            Text(
                text = (designSystem.string(StringToken.FOOTER_SUBTITLE) + appVersion).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = designSystem.color(ColorToken.ACCENT_PRIMARY).copy(alpha = 0.6f),
            )
        }
    }
}
