package application.poligraf.ui.utils

import androidx.compose.runtime.Composable
import application.poligraf.ui.theme.AppStrings
import application.poligraf.ui.theme.CommonStrings
import application.poligraf.ui.theme.DebugStrings
import application.poligraf.ui.theme.ErrorStrings
import application.poligraf.ui.theme.HistoryStrings
import application.poligraf.ui.theme.IAppStrings
import application.poligraf.ui.theme.RecorderStrings
import application.poligraf.ui.theme.SettingsStrings
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
            footerSubtitle = stringResource(provider.common.footerSubtitle),
            save = stringResource(provider.common.save),
            delete = stringResource(provider.common.delete)
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
            rings = stringResource(provider.recorder.rings),
            interpretationCalm = stringResource(provider.recorder.interpretationCalm),
            interpretationPanic = stringResource(provider.recorder.interpretationPanic),
            interpretationAggression = stringResource(provider.recorder.interpretationAggression),
            interpretationConfrontation = stringResource(provider.recorder.interpretationConfrontation),
            interpretationDisorganization = stringResource(provider.recorder.interpretationDisorganization),
            interpretationAnalyzing = stringResource(provider.recorder.interpretationAnalyzing),
            interpretationFormat = stringResource(provider.recorder.interpretationFormat),
            labelStress = stringResource(provider.recorder.labelStress),
            labelPressure = stringResource(provider.recorder.labelPressure),
            labelFear = stringResource(provider.recorder.labelFear),
            labelZero = stringResource(provider.recorder.labelZero),
            labelSyncZone = stringResource(provider.recorder.labelSyncZone),
            timelineTitle = stringResource(provider.recorder.timelineTitle),
            metricJitter = stringResource(provider.recorder.metricJitter),
            metricPitch = stringResource(provider.recorder.metricPitch),
            metricRms = stringResource(provider.recorder.metricRms)
        ),
        history = HistoryStrings(
            empty = stringResource(provider.history.empty),
            itemMarkers = stringResource(provider.history.itemMarkers),
            summaryVolatility = stringResource(provider.history.summaryVolatility),
            summaryDuration = stringResource(provider.history.summaryDuration),
            summaryMarkers = stringResource(provider.history.summaryMarkers),
            volatilityLow = stringResource(provider.history.volatilityLow),
            volatilityMedium = stringResource(provider.history.volatilityMedium),
            volatilityHigh = stringResource(provider.history.volatilityHigh),
            conclusionPositive = stringResource(provider.history.conclusionPositive),
            conclusionNegative = stringResource(provider.history.conclusionNegative),
            conclusionNeutral = stringResource(provider.history.conclusionNeutral),
            labelNotes = stringResource(provider.history.labelNotes),
            notesHint = stringResource(provider.history.notesHint),
            detailTitle = stringResource(provider.history.detailTitle),
            sessionIndexFormat = stringResource(provider.history.sessionIndexFormat)
        ),
        settings = SettingsStrings(
            preferencesTitle = stringResource(provider.settings.preferencesTitle),
            skinTitle = stringResource(provider.settings.skinTitle),
            markerTitle = stringResource(provider.settings.markerTitle)
        ),
        debug = DebugStrings(
            title = stringResource(provider.debug.title),
            triggerLoading = stringResource(provider.debug.triggerLoading),
            triggerError = stringResource(provider.debug.triggerError),
            triggerSuccess = stringResource(provider.debug.triggerSuccess)
        )
    )
}
