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

            StringToken.ERROR_TITLE -> strings.errors.title
            StringToken.ERROR_MESSAGE -> strings.errors.message
            StringToken.RETRY -> strings.errors.retry

            StringToken.RECORDER_TITLE -> strings.recorder.title
            StringToken.ACTIVE_SESSION -> strings.recorder.activeSession
            StringToken.SKIN_STATE_MAP -> strings.recorder.stateMap
            StringToken.SKIN_VOICE_RIBBON -> strings.recorder.voiceRibbon
            StringToken.SKIN_EQUALIZER -> strings.recorder.equalizer
            StringToken.SKIN_RINGS -> strings.recorder.rings

            StringToken.DEBUG_TITLE -> strings.debug.title
            StringToken.DEBUG_TRIGGER_LOADING -> strings.debug.triggerLoading
            StringToken.DEBUG_TRIGGER_ERROR -> strings.debug.triggerError
            StringToken.DEBUG_TRIGGER_SUCCESS -> strings.debug.triggerSuccess
        }
    }
}
