package application.poligraf.ui.theme

import org.jetbrains.compose.resources.StringResource
import application.poligraf.ui.generated.resources.Res
import application.poligraf.ui.generated.resources.*

interface IAppStrings {
    val common: ICommonStrings
    val errors: IErrorStrings
    val recorder: IRecorderStrings
    val debug: IDebugStrings
}

interface ICommonStrings {
    val appName: StringResource
    val welcome1: StringResource
    val welcome2: StringResource
    val welcome3: StringResource
    val welcome4: StringResource
    val settings: StringResource
    val close: StringResource
    val history: StringResource
    val darkMode: StringResource
    val footerTitle: StringResource
    val footerSubtitle: StringResource
}

interface IErrorStrings {
    val title: StringResource
    val message: StringResource
    val retry: StringResource
}

interface IRecorderStrings {
    val title: StringResource
    val activeSession: StringResource
    val stateMap: StringResource
    val voiceRibbon: StringResource
    val equalizer: StringResource
    val rings: StringResource
}

interface IDebugStrings {
    val title: StringResource
    val triggerLoading: StringResource
    val triggerError: StringResource
    val triggerSuccess: StringResource
}

internal class AppStringsImpl : IAppStrings {
    override val common = object : ICommonStrings {
        override val appName = Res.string.app_name
        override val welcome1 = Res.string.welcome_1
        override val welcome2 = Res.string.welcome_2
        override val welcome3 = Res.string.welcome_3
        override val welcome4 = Res.string.welcome_4
        override val settings = Res.string.drawer_settings
        override val close = Res.string.close
        override val history = Res.string.recorder_history_title
        override val darkMode = Res.string.drawer_dark_mode
        override val footerTitle = Res.string.drawer_footer_title
        override val footerSubtitle = Res.string.drawer_footer_subtitle
    }
    
    override val errors = object : IErrorStrings {
        override val title = Res.string.error_unknown_title
        override val message = Res.string.error_unknown_msg
        override val retry = Res.string.error_retry
    }
    
    override val recorder = object : IRecorderStrings {
        override val title = Res.string.recording_screen_title
        override val activeSession = Res.string.recording_screen_placeholder // Temporary mapping
        override val stateMap = Res.string.drawer_settings // Temporary
        override val voiceRibbon = Res.string.drawer_settings // Temporary
        override val equalizer = Res.string.drawer_settings // Temporary
        override val rings = Res.string.drawer_settings // Temporary
    }
    
    override val debug = object : IDebugStrings {
        override val title = Res.string.debug_title
        override val triggerLoading = Res.string.debug_trigger_loading
        override val triggerError = Res.string.debug_trigger_error_blocking
        override val triggerSuccess = Res.string.debug_trigger_success_toast
    }
}
