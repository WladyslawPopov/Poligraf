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
    val save: StringResource
    val delete: StringResource
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
    val interpretationCalm: StringResource
    val interpretationPanic: StringResource
    val interpretationAggression: StringResource
    val interpretationConfrontation: StringResource
    val interpretationDisorganization: StringResource
    val interpretationAnalyzing: StringResource
    val interpretationFormat: StringResource
    val labelStress: StringResource
    val labelPressure: StringResource
    val labelFear: StringResource
    val labelZero: StringResource
    val labelSyncZone: StringResource
    val timelineTitle: StringResource
    val metricJitter: StringResource
    val metricPitch: StringResource
    val metricRms: StringResource
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
        override val save = Res.string.action_save
        override val delete = Res.string.action_delete
    }
    
    override val errors = object : IErrorStrings {
        override val title = Res.string.error_unknown_title
        override val message = Res.string.error_unknown_msg
        override val retry = Res.string.error_retry
    }
    
    override val recorder = object : IRecorderStrings {
        override val title = Res.string.recording_screen_title
        override val activeSession = Res.string.active_session
        override val stateMap = Res.string.skin_state_map
        override val voiceRibbon = Res.string.skin_voice_ribbon
        override val equalizer = Res.string.skin_equalizer
        override val rings = Res.string.skin_rings
        override val interpretationCalm = Res.string.interpretation_calm
        override val interpretationPanic = Res.string.interpretation_panic
        override val interpretationAggression = Res.string.interpretation_aggression
        override val interpretationConfrontation = Res.string.interpretation_confrontation
        override val interpretationDisorganization = Res.string.interpretation_disorganization
        override val interpretationAnalyzing = Res.string.interpretation_analyzing
        override val interpretationFormat = Res.string.interpretation_format
        override val labelStress = Res.string.label_stress
        override val labelPressure = Res.string.label_pressure
        override val labelFear = Res.string.label_fear
        override val labelZero = Res.string.label_zero
        override val labelSyncZone = Res.string.label_sync_zone
        override val timelineTitle = Res.string.timeline_title
        override val metricJitter = Res.string.metric_jitter
        override val metricPitch = Res.string.metric_pitch
        override val metricRms = Res.string.metric_rms
    }
    
    override val debug = object : IDebugStrings {
        override val title = Res.string.debug_title
        override val triggerLoading = Res.string.debug_trigger_loading
        override val triggerError = Res.string.debug_trigger_error_blocking
        override val triggerSuccess = Res.string.debug_trigger_success_toast
    }
}
