package application.liedetector.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import application.liedetector.theme.utils.toComposeColor
import application.liedetector.uicore.theme.DesignSystem
import application.liedetector.uicore.theme.LocalDesignSystem
import application.liedetector.uicore.theme.ResourceProvider
import application.liedetector.uicore.theme.tokens.ColorToken
import org.koin.compose.koinInject

@Composable
fun LieDetectorTheme(
    content: @Composable () -> Unit
) {
    val darkTheme by ThemeState.isDark.collectAsState()
    val resources: ResourceProvider = koinInject()
    val injectedDS: DesignSystem = koinInject()
    
    val designSystem = remember(darkTheme) { 
        DesignSystem(resources, darkTheme, isDebug = injectedDS.isDebug) 
    }
    
    // Map our tokens to Material 3 ColorScheme for standard components
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = designSystem.color(ColorToken.PRIMARY).toComposeColor(),
            background = designSystem.color(ColorToken.BACKGROUND).toComposeColor(),
            surface = designSystem.color(ColorToken.SURFACE).toComposeColor(),
            error = designSystem.color(ColorToken.ERROR).toComposeColor()
        )
    } else {
        lightColorScheme(
            primary = designSystem.color(ColorToken.PRIMARY).toComposeColor(),
            background = designSystem.color(ColorToken.BACKGROUND).toComposeColor(),
            surface = designSystem.color(ColorToken.SURFACE).toComposeColor(),
            error = designSystem.color(ColorToken.ERROR).toComposeColor()
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
