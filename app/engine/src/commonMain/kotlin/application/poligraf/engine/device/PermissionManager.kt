package application.poligraf.engine.device

import kotlinx.coroutines.flow.StateFlow

enum class AppPermission {
    RECORD_AUDIO
}

interface PermissionManager {
    val permissionsState: StateFlow<Map<AppPermission, Boolean>>
    
    fun isGranted(permission: AppPermission): Boolean
    
    /**
     * Triggers the platform-specific permission request.
     * On Android, this should be handled by the Activity.
     */
    fun requestPermission(permission: AppPermission)
}
