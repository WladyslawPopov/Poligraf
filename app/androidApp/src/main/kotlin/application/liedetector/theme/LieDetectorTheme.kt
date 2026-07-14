package application.liedetector.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import application.liedetector.theme.utils.toComposeColor
import application.liedetector.uicore.theme.ColorToken
import application.liedetector.uicore.theme.DesignSystem
import application.liedetector.uicore.theme.LocalDesignSystem
import org.koin.compose.koinInject

@Composable
fun LieDetectorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val designSystem: DesignSystem = koinInject()
    
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
