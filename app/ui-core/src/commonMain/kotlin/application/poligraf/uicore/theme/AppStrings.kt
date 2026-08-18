package application.poligraf.uicore.theme

import androidx.compose.runtime.Immutable

/**
 * Clean UI Strings (resolved from resources).
 */
@Immutable
data class AppStrings(
    val common: CommonStrings,
    val errors: ErrorStrings,
    val recorder: RecorderStrings,
    val drawer: DrawerStrings,
    val debug: DebugStrings,
    val subjects: SubjectStrings,
    val toast: ToastStrings
)

@Immutable
data class CommonStrings(
    val appName: String,
    val welcomeText: String,
    val record: String,
    val close: String,
    val menu: String,
    val ok: String,
    val cancel: String,
    val unknown: String
)

@Immutable
data class ErrorStrings(
    val noInternetTitle: String,
    val noInternetMsg: String,
    val serverTitle: String,
    val serverMsg: String,
    val unknownTitle: String,
    val unknownMsg: String,
    val retry: String
)

@Immutable
data class RecorderStrings(
    val title: String,
    val placeholder: String,
    val replace: String,
    val trim: String,
    val trimCancel: String,
    val trimDone: String,
    val trimApply: String,
    val deletePart: String,
    val save: String,
    val edit: String,
    val uploadFile: String,
    val trimMode: String,
    val historyTitle: String,
    val historySelect: String,
    val historyEmpty: String,
    val today: String,
    val delete: String
)

@Immutable
data class DrawerStrings(
    val settings: String,
    val darkMode: String,
    val openDebug: String,
    val footerTitle: String,
    val footerSubtitle: String
)

@Immutable
data class DebugStrings(
    val title: String,
    val dashboard: String,
    val triggerLoading: String,
    val triggerErrorBlocking: String,
    val triggerErrorToast: String,
    val triggerSuccessToast: String,
    val tabStates: String,
    val tabWidgets: String,
    val tabLabs: String,
    val labsEmpty: String
)

@Immutable
data class SubjectStrings(
    val newTitle: String,
    val newButton: String,
    val sectionTemplates: String,
    val sectionRecordings: String,
    val actionSelected: String,
    val deleteConfirmation: String,
    val actionDelete: String,
    val analysisScore: String
)

@Immutable
data class ToastStrings(
    val authSuccess: String,
    val authFailed: String,
    val genericWarning: String
)

// Aliases for compatibility during migration if needed
typealias AppUIStrings = AppStrings
typealias CommonUIStrings = CommonStrings
typealias ErrorUIStrings = ErrorStrings
typealias RecorderUIStrings = RecorderStrings
typealias DrawerUIStrings = DrawerStrings
typealias DebugUIStrings = DebugStrings
typealias SubjectUIStrings = SubjectStrings
typealias ToastUIStrings = ToastStrings
