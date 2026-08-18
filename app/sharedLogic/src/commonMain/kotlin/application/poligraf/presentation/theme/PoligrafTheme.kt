package application.poligraf.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import application.poligraf.widgets.utils.composeColor
import application.poligraf.uicore.theme.DesignSystem
import application.poligraf.uicore.theme.LocalDesignSystem
import application.poligraf.uicore.theme.ResourceProvider
import application.poligraf.uicore.theme.tokens.ColorToken
import org.koin.compose.koinInject

@Composable
fun PoligrafTheme(
    content: @Composable () -> Unit
) {
    val darkTheme by ThemeState.isDark.collectAsState()
    val resources: ResourceProvider = koinInject()
    val injectedDS: DesignSystem = koinInject()
    
    val designSystem = remember(darkTheme) { 
        DesignSystem(resources, darkTheme, isDebug = injectedDS.isDebug) 
    }
    
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = designSystem.composeColor(ColorToken.PRIMARY),
            background = designSystem.composeColor(ColorToken.BACKGROUND),
            surface = designSystem.composeColor(ColorToken.SURFACE),
            error = designSystem.composeColor(ColorToken.ERROR)
        )
    } else {
        lightColorScheme(
            primary = designSystem.composeColor(ColorToken.PRIMARY),
            background = designSystem.composeColor(ColorToken.BACKGROUND),
            surface = designSystem.composeColor(ColorToken.SURFACE),
            error = designSystem.composeColor(ColorToken.ERROR)
        )
    }

    CompositionLocalProvider(
        LocalDesignSystem provides designSystem
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
