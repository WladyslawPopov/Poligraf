package application.poligraf.uicore.theme

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource

@Composable
fun rememberAppUIStrings(provider: IAppStrings): AppStrings {
    return AppStrings(
        common = CommonStrings(
            appName = stringResource(provider.common.appName),
            welcomeText = stringResource(provider.common.welcomeText),
            record = stringResource(provider.common.record),
            close = stringResource(provider.common.close),
            menu = stringResource(provider.common.menu),
            ok = "OK",
            cancel = stringResource(provider.recorder.cancel),
            unknown = stringResource(provider.common.unknown)
        ),
        errors = ErrorStrings(
            noInternetTitle = stringResource(provider.errors.noInternetTitle),
            noInternetMsg = stringResource(provider.errors.noInternetMsg),
            serverTitle = stringResource(provider.errors.serverTitle),
            serverMsg = stringResource(provider.errors.serverMsg),
            unknownTitle = stringResource(provider.errors.unknownTitle),
            unknownMsg = stringResource(provider.errors.unknownMsg),
            retry = stringResource(provider.errors.retry)
        ),
        recorder = RecorderStrings(
            title = stringResource(provider.recorder.title),
            placeholder = stringResource(provider.recorder.placeholder),
            replace = stringResource(provider.recorder.replace),
            trim = stringResource(provider.recorder.trim),
            trimCancel = stringResource(provider.recorder.trimCancel),
            trimDone = stringResource(provider.recorder.trimDone),
            trimApply = stringResource(provider.recorder.trimApply),
            deletePart = stringResource(provider.recorder.deletePart),
            save = stringResource(provider.recorder.save),
            edit = stringResource(provider.recorder.edit),
            uploadFile = stringResource(provider.recorder.uploadFile),
            trimMode = stringResource(provider.recorder.trimMode),
            historyTitle = stringResource(provider.recorder.historyTitle),
            historySelect = stringResource(provider.recorder.historySelect),
            historyEmpty = stringResource(provider.recorder.historyEmpty),
            today = stringResource(provider.recorder.today),
            delete = stringResource(provider.recorder.delete)
        ),
        drawer = DrawerStrings(
            settings = stringResource(provider.drawer.settings),
            darkMode = stringResource(provider.drawer.darkMode),
            openDebug = stringResource(provider.drawer.openDebug),
            footerTitle = stringResource(provider.drawer.footerTitle),
            footerSubtitle = stringResource(provider.drawer.footerSubtitle)
        ),
        debug = DebugStrings(
            title = stringResource(provider.debug.title),
            dashboard = stringResource(provider.debug.dashboard),
            triggerLoading = stringResource(provider.debug.triggerLoading),
            triggerErrorBlocking = stringResource(provider.debug.triggerErrorBlocking),
            triggerErrorToast = stringResource(provider.debug.triggerErrorToast),
            triggerSuccessToast = stringResource(provider.debug.triggerSuccessToast),
            tabStates = stringResource(provider.debug.tabStates),
            tabWidgets = stringResource(provider.debug.tabWidgets),
            tabLabs = stringResource(provider.debug.tabLabs),
            labsEmpty = stringResource(provider.debug.labsEmpty)
        ),
        subjects = SubjectStrings(
            newTitle = stringResource(provider.subjects.newTitle),
            newButton = stringResource(provider.subjects.newButton),
            sectionTemplates = stringResource(provider.subjects.sectionTemplates),
            sectionRecordings = stringResource(provider.subjects.sectionRecordings),
            actionSelected = stringResource(provider.subjects.actionSelected),
            deleteConfirmation = stringResource(provider.subjects.deleteConfirmation),
            actionDelete = stringResource(provider.subjects.actionDelete),
            analysisScore = stringResource(provider.subjects.analysisScore)
        ),
        toast = ToastStrings(
            authSuccess = stringResource(provider.toast.authSuccess),
            authFailed = stringResource(provider.toast.authFailed),
            genericWarning = stringResource(provider.toast.genericWarning)
        )
    )
}
