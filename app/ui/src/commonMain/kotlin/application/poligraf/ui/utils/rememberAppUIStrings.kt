package application.poligraf.ui.utils

import androidx.compose.runtime.Composable
import application.poligraf.ui.theme.AppStrings
import application.poligraf.ui.theme.CommonStrings
import application.poligraf.ui.theme.DebugStrings
import application.poligraf.ui.theme.ErrorStrings
import application.poligraf.ui.theme.IAppStrings
import application.poligraf.ui.theme.RecorderStrings
import org.jetbrains.compose.resources.stringResource

@Composable
fun rememberAppUIStrings(provider: IAppStrings): AppStrings {
    return AppStrings(
        common = CommonStrings(
            appName = stringResource(provider.common.appName),
            welcome1 = stringResource(provider.common.welcome1),
            welcome2 = stringResource(provider.common.welcome2),
            welcome3 = stringResource(provider.common.welcome3),
            welcome4 = stringResource(provider.common.welcome4),
            settings = stringResource(provider.common.settings),
            close = stringResource(provider.common.close),
            history = stringResource(provider.common.history),
            darkMode = stringResource(provider.common.darkMode),
            footerTitle = stringResource(provider.common.footerTitle),
            footerSubtitle = stringResource(provider.common.footerSubtitle)
        ),
        errors = ErrorStrings(
            title = stringResource(provider.errors.title),
            message = stringResource(provider.errors.message),
            retry = stringResource(provider.errors.retry)
        ),
        recorder = RecorderStrings(
            title = stringResource(provider.recorder.title),
            activeSession = stringResource(provider.recorder.activeSession),
            stateMap = stringResource(provider.recorder.stateMap),
            voiceRibbon = stringResource(provider.recorder.voiceRibbon),
            equalizer = stringResource(provider.recorder.equalizer),
            rings = stringResource(provider.recorder.rings)
        ),
        debug = DebugStrings(
            title = stringResource(provider.debug.title),
            triggerLoading = stringResource(provider.debug.triggerLoading),
            triggerError = stringResource(provider.debug.triggerError),
            triggerSuccess = stringResource(provider.debug.triggerSuccess)
        )
    )
}
