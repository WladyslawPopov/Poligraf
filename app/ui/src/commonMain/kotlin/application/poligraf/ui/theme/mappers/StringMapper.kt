package application.poligraf.ui.theme.mappers

import application.poligraf.ui.theme.AppStrings
import application.poligraf.ui.theme.tokens.StringToken

internal object StringMapper {
    fun getString(token: StringToken, strings: AppStrings): String {
        return when(token) {
            StringToken.APP_NAME -> strings.common.appName
            StringToken.WELCOME_1 -> strings.common.welcome1
            StringToken.WELCOME_2 -> strings.common.welcome2
            StringToken.WELCOME_3 -> strings.common.welcome3
            StringToken.WELCOME_4 -> strings.common.welcome4
            StringToken.SETTINGS -> strings.common.settings
            StringToken.CLOSE -> strings.common.close
            StringToken.HISTORY -> strings.common.history
            StringToken.DARK_MODE -> strings.common.darkMode
            StringToken.FOOTER_TITLE -> strings.common.footerTitle
            StringToken.FOOTER_SUBTITLE -> strings.common.footerSubtitle
            StringToken.SAVE -> strings.common.save
            StringToken.DELETE -> strings.common.delete

            StringToken.ERROR_TITLE -> strings.errors.title
            StringToken.ERROR_MESSAGE -> strings.errors.message
            StringToken.RETRY -> strings.errors.retry

            StringToken.RECORDER_TITLE -> strings.recorder.title
            StringToken.ACTIVE_SESSION -> strings.recorder.activeSession
            StringToken.SKIN_STATE_MAP -> strings.recorder.stateMap
            StringToken.SKIN_VOICE_RIBBON -> strings.recorder.voiceRibbon
            StringToken.SKIN_EQUALIZER -> strings.recorder.equalizer
            StringToken.SKIN_RINGS -> strings.recorder.rings

            StringToken.LABEL_STRESS -> strings.recorder.labelStress
            StringToken.LABEL_PRESSURE -> strings.recorder.labelPressure
            StringToken.LABEL_FEAR -> strings.recorder.labelFear
            StringToken.LABEL_ZERO -> strings.recorder.labelZero
            StringToken.LABEL_SYNC_ZONE -> strings.recorder.labelSyncZone
            StringToken.TIMELINE_TITLE -> strings.recorder.timelineTitle
            StringToken.INTERPRETATION_CALM -> strings.recorder.interpretationCalm
            StringToken.INTERPRETATION_PANIC -> strings.recorder.interpretationPanic
            StringToken.INTERPRETATION_AGGRESSION -> strings.recorder.interpretationAggression
            StringToken.INTERPRETATION_CONFRONTATION -> strings.recorder.interpretationConfrontation
            StringToken.INTERPRETATION_DISORGANIZATION -> strings.recorder.interpretationDisorganization
            StringToken.INTERPRETATION_ANALYZING -> strings.recorder.interpretationAnalyzing
            StringToken.INTERPRETATION_FORMAT -> strings.recorder.interpretationFormat
            StringToken.METRIC_JITTER -> strings.recorder.metricJitter
            StringToken.METRIC_PITCH -> strings.recorder.metricPitch
            StringToken.METRIC_RMS -> strings.recorder.metricRms

            StringToken.DEBUG_TITLE -> strings.debug.title
            StringToken.DEBUG_TRIGGER_LOADING -> strings.debug.triggerLoading
            StringToken.DEBUG_TRIGGER_ERROR -> strings.debug.triggerError
            StringToken.DEBUG_TRIGGER_SUCCESS -> strings.debug.triggerSuccess

            StringToken.HISTORY_EMPTY -> strings.history.empty
            StringToken.HISTORY_ITEM_MARKERS -> strings.history.itemMarkers
            StringToken.HISTORY_SUMMARY_VOLATILITY -> strings.history.summaryVolatility
            StringToken.HISTORY_SUMMARY_DURATION -> strings.history.summaryDuration
            StringToken.HISTORY_SUMMARY_MARKERS -> strings.history.summaryMarkers
            StringToken.VOLATILITY_LOW -> strings.history.volatilityLow
            StringToken.VOLATILITY_MEDIUM -> strings.history.volatilityMedium
            StringToken.VOLATILITY_HIGH -> strings.history.volatilityHigh
            StringToken.CONCLUSION_POSITIVE -> strings.history.conclusionPositive
            StringToken.CONCLUSION_NEGATIVE -> strings.history.conclusionNegative
            StringToken.CONCLUSION_NEUTRAL -> strings.history.conclusionNeutral
            StringToken.LABEL_NOTES -> strings.history.labelNotes
            StringToken.NOTES_HINT -> strings.history.notesHint
            StringToken.HISTORY_DETAIL_TITLE -> strings.history.detailTitle

            StringToken.SETTINGS_PREFERENCES_TITLE -> strings.settings.preferencesTitle
            StringToken.SETTINGS_SKIN_TITLE -> strings.settings.skinTitle
            StringToken.SETTINGS_MARKER_TITLE -> strings.settings.markerTitle
        }
    }
}
