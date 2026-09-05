package application.poligraf.ui.theme

import org.jetbrains.compose.resources.StringResource
import application.poligraf.ui.generated.resources.Res
import application.poligraf.ui.generated.resources.*

interface IAppStrings {
    val common: ICommonStrings
    val errors: IErrorStrings
    val recorder: IRecorderStrings
    val history: IHistoryStrings
    val settings: ISettingsStrings
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
    val synthesizing: StringResource
    val stateMap: StringResource
    val voiceRibbon: StringResource
    val equalizer: StringResource
    val rings: StringResource
    val statusWarmup: StringResource
    val statusClipping: StringResource
    val statusLowSnr: StringResource
    val statusCalm: StringResource
    val statusMildFluctuation: StringResource
    val statusFearSingle: StringResource
    val statusStressSingle: StringResource
    val statusPressureSingle: StringResource
    val interpretationCalm: StringResource
    val interpretationPanic: StringResource
    val interpretationAggression: StringResource
    val interpretationConfrontation: StringResource
    val interpretationDisorganization: StringResource
    val interpretationAnalyzing: StringResource
    val interpretationFormat: StringResource
    val labelSyncZone: StringResource
    val timelineTitle: StringResource
    val metricJitter: StringResource
    val metricPitch: StringResource
    val metricRms: StringResource
}

interface IHistoryStrings {
    val empty: StringResource
    val itemMarkers: StringResource
    val itemNotes: StringResource
    val summaryVolatility: StringResource
    val summaryDuration: StringResource
    val summaryMarkers: StringResource
    val volatilityLow: StringResource
    val volatilityMedium: StringResource
    val volatilityHigh: StringResource
    val conclusionPositive: StringResource
    val conclusionNegative: StringResource
    val conclusionNeutral: StringResource
    val conclusionUnreliable: StringResource
    val labelNotes: StringResource
    val notesHint: StringResource
    val detailTitle: StringResource
    val sessionIndexFormat: StringResource
}

interface ISettingsStrings {
    val preferencesTitle: StringResource
    val skinTitle: StringResource
    val markerTitle: StringResource
    val quantumWindowTitle: StringResource
    val sensitivityTitle: StringResource
    val sensitivityLow: StringResource
    val sensitivityMedium: StringResource
    val sensitivityHigh: StringResource
}

interface IDebugStrings {
    val title: StringResource
    val triggerLoading: StringResource
    val triggerError: StringResource
    val triggerSuccess: StringResource
}

class AppStringsImpl : IAppStrings {
    override val common = object : ICommonStrings {
        override val appName = Res.string.app_name
        override val welcome1 = Res.string.welcome_1
        override val welcome2 = Res.string.welcome_2
        override val welcome3 = Res.string.welcome_3
        override val welcome4 = Res.string.welcome_4
        override val settings = Res.string.title_settings
        override val close = Res.string.action_close
        override val history = Res.string.title_history
        override val darkMode = Res.string.label_dark_mode
        override val footerTitle = Res.string.drawer_footer_title
        override val footerSubtitle = Res.string.drawer_footer_subtitle
        override val save = Res.string.action_save
        override val delete = Res.string.action_delete
    }

    override val errors = object : IErrorStrings {
        override val title = Res.string.error_title
        override val message = Res.string.error_message
        override val retry = Res.string.action_retry
    }

    override val recorder = object : IRecorderStrings {
        override val title = Res.string.analyzer_title
        override val activeSession = Res.string.active_session
        override val synthesizing = Res.string.analysis_synthesizing
        override val stateMap = Res.string.skin_state_map
        override val voiceRibbon = Res.string.skin_voice_ribbon
        override val equalizer = Res.string.skin_equalizer
        override val rings = Res.string.skin_rings
        override val statusWarmup = Res.string.status_warmup
        override val statusClipping = Res.string.status_clipping
        override val statusLowSnr = Res.string.status_low_snr
        override val statusCalm = Res.string.status_calm
        override val statusMildFluctuation = Res.string.status_mild_fluctuation
        override val statusFearSingle = Res.string.status_fear_single
        override val statusStressSingle = Res.string.status_stress_single
        override val statusPressureSingle = Res.string.status_pressure_single
        override val interpretationCalm = Res.string.interpretation_calm
        override val interpretationPanic = Res.string.interpretation_panic
        override val interpretationAggression = Res.string.interpretation_aggression
        override val interpretationConfrontation = Res.string.interpretation_confrontation
        override val interpretationDisorganization = Res.string.interpretation_disorganization
        override val interpretationAnalyzing = Res.string.interpretation_analyzing
        override val interpretationFormat = Res.string.interpretation_format
        override val labelSyncZone = Res.string.label_sync_zone
        override val timelineTitle = Res.string.timeline_title
        override val metricJitter = Res.string.metric_jitter
        override val metricPitch = Res.string.metric_pitch
        override val metricRms = Res.string.metric_rms
    }

    override val history = object : IHistoryStrings {
        override val empty = Res.string.history_empty
        override val itemMarkers = Res.string.history_item_markers
        override val itemNotes = Res.string.history_item_notes
        override val summaryVolatility = Res.string.history_summary_volatility
        override val summaryDuration = Res.string.history_summary_duration
        override val summaryMarkers = Res.string.history_summary_markers
        override val volatilityLow = Res.string.volatility_low
        override val volatilityMedium = Res.string.volatility_medium
        override val volatilityHigh = Res.string.volatility_high
        override val conclusionPositive = Res.string.conclusion_positive
        override val conclusionNegative = Res.string.conclusion_negative
        override val conclusionNeutral = Res.string.conclusion_neutral
        override val conclusionUnreliable = Res.string.conclusion_unreliable
        override val labelNotes = Res.string.label_notes
        override val notesHint = Res.string.notes_hint
        override val detailTitle = Res.string.history_detail_title
        override val sessionIndexFormat = Res.string.session_index_format
    }

    override val settings = object : ISettingsStrings {
        override val preferencesTitle = Res.string.settings_preferences_title
        override val skinTitle = Res.string.settings_skin_title
        override val markerTitle = Res.string.settings_marker_title
        override val quantumWindowTitle = Res.string.settings_quantum_window_title
        override val sensitivityTitle = Res.string.settings_sensitivity_title
        override val sensitivityLow = Res.string.sensitivity_low
        override val sensitivityMedium = Res.string.sensitivity_medium
        override val sensitivityHigh = Res.string.sensitivity_high
    }

    override val debug = object : IDebugStrings {
        override val title = Res.string.debug_title
        override val triggerLoading = Res.string.debug_trigger_loading
        override val triggerError = Res.string.debug_trigger_error
        override val triggerSuccess = Res.string.debug_trigger_success
    }
}
