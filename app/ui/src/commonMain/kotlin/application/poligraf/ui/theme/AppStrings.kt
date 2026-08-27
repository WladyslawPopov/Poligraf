package application.poligraf.ui.theme

import androidx.compose.runtime.Immutable

/**
 * Resolved UI Strings.
 */
@Immutable
data class AppStrings(
    val common: CommonStrings,
    val errors: ErrorStrings,
    val recorder: RecorderStrings,
    val history: HistoryStrings,
    val settings: SettingsStrings,
    val debug: DebugStrings
)

@Immutable
data class CommonStrings(
    val appName: String,
    val welcome1: String,
    val welcome2: String,
    val welcome3: String,
    val welcome4: String,
    val settings: String,
    val close: String,
    val history: String,
    val darkMode: String,
    val footerTitle: String,
    val footerSubtitle: String,
    val save: String,
    val delete: String
)

@Immutable
data class ErrorStrings(
    val title: String,
    val message: String,
    val retry: String
)

@Immutable
data class RecorderStrings(
    val title: String,
    val activeSession: String,
    val stateMap: String,
    val voiceRibbon: String,
    val equalizer: String,
    val rings: String,
    val interpretationCalm: String,
    val interpretationPanic: String,
    val interpretationAggression: String,
    val interpretationConfrontation: String,
    val interpretationDisorganization: String,
    val interpretationAnalyzing: String,
    val interpretationFormat: String,
    val labelStress: String,
    val labelPressure: String,
    val labelFear: String,
    val labelZero: String,
    val labelSyncZone: String,
    val timelineTitle: String,
    val metricJitter: String,
    val metricPitch: String,
    val metricRms: String
)

@Immutable
data class DebugStrings(
    val title: String,
    val triggerLoading: String,
    val triggerError: String,
    val triggerSuccess: String
)

@Immutable
data class HistoryStrings(
    val empty: String,
    val itemMarkers: String,
    val summaryVolatility: String,
    val summaryDuration: String,
    val summaryMarkers: String,
    val volatilityLow: String,
    val volatilityMedium: String,
    val volatilityHigh: String,
    val conclusionPositive: String,
    val conclusionNegative: String,
    val conclusionNeutral: String,
    val labelNotes: String,
    val notesHint: String,
    val detailTitle: String
)

@Immutable
data class SettingsStrings(
    val preferencesTitle: String,
    val skinTitle: String,
    val markerTitle: String
)
