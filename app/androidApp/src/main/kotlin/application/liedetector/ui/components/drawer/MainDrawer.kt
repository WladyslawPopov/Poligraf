package application.liedetector.ui.components.drawer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import application.liedetector.theme.ThemeState
import application.liedetector.theme.utils.composeColor
import application.liedetector.uicore.theme.DesignSystem
import application.liedetector.engine.navigation.AppNavigation
import application.liedetector.uicore.theme.tokens.ColorToken
import application.liedetector.uicore.theme.tokens.StringToken
import application.liedetector.uicore.theme.tokens.DimenToken
import application.liedetector.presentation.main.MainState

@Composable
fun MainDrawer(
    state: MainState,
    designSystem: DesignSystem,
    navigation: AppNavigation
){
    ModalDrawerSheet(
        drawerContainerColor = Color.Transparent,
        drawerShape = RoundedCornerShape(0.dp), 
        windowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.fillMaxHeight().width(designSystem.dimen(DimenToken.DRAWER_WIDTH).dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = designSystem.composeColor(ColorToken.SURFACE).copy(alpha = 0.92f),
            shape = RoundedCornerShape(
                topEnd = designSystem.dimen(DimenToken.DRAWER_CORNER).dp, 
                bottomEnd = designSystem.dimen(DimenToken.DRAWER_CORNER).dp
            ),
            border = BorderStroke(
                designSystem.dimen(DimenToken.DIVIDER_THICKNESS).dp, 
                Brush.horizontalGradient(
                    listOf(Color.Transparent, designSystem.composeColor(ColorToken.ACCENT_PRIMARY).copy(alpha = 0.3f))
                )
            )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Area with Accent Background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    designSystem.composeColor(ColorToken.ACCENT_PRIMARY).copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                        .statusBarsPadding()
                        .padding(
                            top = designSystem.dimen(DimenToken.SPACING_MEDIUM).dp, 
                            bottom = designSystem.dimen(DimenToken.SPACING_LARGE).dp, 
                            start = designSystem.dimen(DimenToken.SPACING_LARGE).dp, 
                            end = designSystem.dimen(DimenToken.SPACING_LARGE).dp
                        )
                ) {
                    Text(
                        text = designSystem.string(StringToken.DRAWER_SETTINGS),
                        style = MaterialTheme.typography.headlineMedium,
                        color = designSystem.composeColor(ColorToken.ACCENT_PRIMARY),
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                
                GlassDivider(designSystem)

                Spacer(modifier = Modifier.height(designSystem.dimen(DimenToken.SPACING_MEDIUM).dp))

                // Settings Content
                val isDark by ThemeState.isDark.collectAsState()

                DrawerItem(
                    label = designSystem.string(StringToken.DRAWER_DARK_MODE),
                    designSystem = designSystem,
                    trailing = {
                        Switch(
                            checked = isDark,
                            onCheckedChange = { ThemeState.toggle() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = designSystem.composeColor(ColorToken.ACCENT_PRIMARY),
                                checkedTrackColor = designSystem.composeColor(ColorToken.ACCENT_PRIMARY).copy(alpha = 0.3f),
                                uncheckedBorderColor = designSystem.composeColor(ColorToken.TEXT_SECONDARY).copy(alpha = 0.5f)
                            )
                        )
                    }
                )

                if (designSystem.isDebug) {
                    Spacer(modifier = Modifier.height(designSystem.dimen(DimenToken.SPACING_SMALL).dp))
                    GlassDivider(designSystem)
                    Spacer(modifier = Modifier.height(designSystem.dimen(DimenToken.SPACING_SMALL).dp))
                    
                    DrawerItem(
                        label = designSystem.string(StringToken.OPEN_DEBUG_SANDBOX),
                        designSystem = designSystem,
                        color = designSystem.composeColor(ColorToken.ACCENT_ENERGY),
                        onClick = {
                            navigation.setDrawerOpen(false)
                            navigation.openDebug()
                        }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                
                // Footer Info
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(designSystem.dimen(DimenToken.SPACING_LARGE).dp)
                ) {
                    Text(
                        text = designSystem.string(StringToken.DRAWER_FOOTER_TITLE).uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = designSystem.composeColor(ColorToken.TEXT_PRIMARY),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = TextUnit.Unspecified
                    )
                    Text(
                        text = (designSystem.string(StringToken.DRAWER_FOOTER_SUBTITLE) + (state.appConfig?.appVersion ?: "1.0.0")).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = designSystem.composeColor(ColorToken.ACCENT_PRIMARY).copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerItem(
    label: String,
    designSystem: DesignSystem,
    color: Color? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(designSystem.dimen(DimenToken.HEADER_HEIGHT).dp)
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = designSystem.dimen(DimenToken.SPACING_LARGE).dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = color ?: designSystem.composeColor(ColorToken.TEXT_PRIMARY),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        trailing?.invoke()
    }
}

@Composable
private fun GlassDivider(designSystem: DesignSystem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(designSystem.dimen(DimenToken.DIVIDER_THICKNESS).dp)
            .padding(horizontal = designSystem.dimen(DimenToken.SPACING_LARGE).dp)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        designSystem.composeColor(ColorToken.ACCENT_PRIMARY).copy(alpha = 0.3f),
                        Color.Transparent
                    )
                )
            )
    )
}
