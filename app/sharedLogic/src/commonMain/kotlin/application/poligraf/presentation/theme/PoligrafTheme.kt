package application.poligraf.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import application.poligraf.domain.preferences.repository.PreferencesRepository
import application.poligraf.ui.theme.DesignSystem
import application.poligraf.ui.theme.IAppStrings
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.utils.rememberAppUIStrings
import org.koin.compose.koinInject

@Composable
fun PoligrafTheme(
    preferencesRepository: PreferencesRepository = koinInject(),
    appStrings: IAppStrings = koinInject(),
    content: @Composable () -> Unit,
) {
    val isDark by preferencesRepository.isDarkModeFlow.collectAsState(true)
    val strings = rememberAppUIStrings(appStrings)

    val designSystem = DesignSystem(
        strings = strings,
        isDark = isDark,
        isDebug = true
    )

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = designSystem.color(ColorToken.ACCENT_PRIMARY),
            background = designSystem.color(ColorToken.SURFACE_BACKGROUND),
            surface = designSystem.color(ColorToken.SURFACE_PRIMARY),
            error = designSystem.color(ColorToken.STATE_ERROR)
        )
    } else {
        lightColorScheme(
            primary = designSystem.color(ColorToken.ACCENT_PRIMARY),
            background = designSystem.color(ColorToken.SURFACE_BACKGROUND),
            surface = designSystem.color(ColorToken.SURFACE_PRIMARY),
            error = designSystem.color(ColorToken.STATE_ERROR)
        )
    }

    val shapes = Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(12.dp),
        large = RoundedCornerShape(16.dp),
        extraLarge = RoundedCornerShape(24.dp)
    )

    CompositionLocalProvider(
        LocalDesignSystem provides designSystem
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            shapes = shapes,
            content = content
        )
    }
}
