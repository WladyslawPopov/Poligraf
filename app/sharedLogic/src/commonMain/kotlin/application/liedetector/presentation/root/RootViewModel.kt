package application.liedetector.presentation.root

import application.liedetector.data.user.UserRepository
import application.liedetector.engine.device.DeviceInfoProvider
import application.liedetector.presentation.base.BaseViewModel

class RootViewModel(
    private val userRepository: UserRepository,
    private val deviceProvider: DeviceInfoProvider
) : BaseViewModel() {

    init {
        initializeApp()
    }

    private fun initializeApp() {
        launchSafe(
            block = {
                // 1. Ensure user is authorized anonymously
                userRepository.loginAnonymously()
                
                // 2. Collect metadata and sync with server
                val metadata = mapOf(
                    "device_id" to deviceProvider.getDeviceId(),
                    "device_model" to deviceProvider.getDeviceModel(),
                    "os_version" to deviceProvider.getOsVersion(),
                    "language" to deviceProvider.getLanguage(),
                    "region" to deviceProvider.getRegion(),
                    "timezone" to deviceProvider.getTimeZone(),
                    "app_version" to deviceProvider.getAppVersion()
                )
                
                userRepository.syncUser(metadata)
            }
        )
    }
}
