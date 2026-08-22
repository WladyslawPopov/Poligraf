package application.poligraf.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import application.poligraf.uicore.theme.DesignSystem
import application.poligraf.uicore.theme.LocalDesignSystem
import application.poligraf.uicore.theme.IAppStrings
import application.poligraf.uicore.theme.mappers.rememberAppUIStrings
import application.poligraf.uicore.theme.tokens.ColorToken
import org.koin.compose.koinInject

@Composable
fun PoligrafTheme(
    content: @Composable () -> Unit
) {
    val darkTheme by ThemeState.isDark.collectAsState()
    val appConfig: application.poligraf.engine.config.AppConfig = koinInject()
    
    val stringProvider: IAppStrings = koinInject()
    val strings = rememberAppUIStrings(stringProvider)
    val designSystem = remember(darkTheme, strings) { 
        DesignSystem(strings, darkTheme, isDebug = appConfig.isDebug)
    }
    
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = designSystem.color(ColorToken.PRIMARY),
            background = designSystem.color(ColorToken.BACKGROUND),
            surface = designSystem.color(ColorToken.SURFACE),
            error = designSystem.color(ColorToken.ERROR)
        )
    } else {
        lightColorScheme(
            primary = designSystem.color(ColorToken.PRIMARY),
            background = designSystem.color(ColorToken.BACKGROUND),
            surface = designSystem.color(ColorToken.SURFACE),
            error = designSystem.color(ColorToken.ERROR)
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
