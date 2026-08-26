package application.poligraf.engine.device

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AndroidPermissionManager(private val context: Context) : PermissionManager {
    private val _permissionsState = MutableStateFlow<Map<AppPermission, Boolean>>(
        AppPermission.entries.associateWith { isGrantedInternal(it) }
    )
    override val permissionsState: StateFlow<Map<AppPermission, Boolean>> = _permissionsState.asStateFlow()

    private var requestAction: ((AppPermission) -> Unit)? = null

    fun setRequestAction(action: (AppPermission) -> Unit) {
        requestAction = action
    }

    fun notifyPermissionResult(permission: AppPermission, isGranted: Boolean) {
        _permissionsState.update { it + (permission to isGranted) }
    }

    override fun isGranted(permission: AppPermission): Boolean {
        val granted = isGrantedInternal(permission)
        if (granted != _permissionsState.value[permission]) {
            notifyPermissionResult(permission, granted)
        }
        return granted
    }

    private fun isGrantedInternal(permission: AppPermission): Boolean {
        val androidPermission = when (permission) {
            AppPermission.RECORD_AUDIO -> Manifest.permission.RECORD_AUDIO
        }
        return ContextCompat.checkSelfPermission(context, androidPermission) == PackageManager.PERMISSION_GRANTED
    }

    override fun requestPermission(permission: AppPermission) {
        if (isGranted(permission)) {
            notifyPermissionResult(permission, true)
            return
        }
        requestAction?.invoke(permission)
    }
}
