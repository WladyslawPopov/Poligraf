package application.poligraf.engine.device

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted

class IosPermissionManager : PermissionManager {
    private val _permissionsState = MutableStateFlow<Map<AppPermission, Boolean>>(
        AppPermission.entries.associateWith { isGranted(it) }
    )
    override val permissionsState: StateFlow<Map<AppPermission, Boolean>> = _permissionsState.asStateFlow()

    override fun isGranted(permission: AppPermission): Boolean {
        return when (permission) {
            AppPermission.RECORD_AUDIO -> {
                AVAudioSession.sharedInstance().recordPermission() == AVAudioSessionRecordPermissionGranted
            }
        }
    }

    override fun requestPermission(permission: AppPermission) {
        when (permission) {
            AppPermission.RECORD_AUDIO -> {
                AVAudioSession.sharedInstance().requestRecordPermission { granted ->
                    _permissionsState.update { it + (permission to granted) }
                }
            }
        }
    }
}
