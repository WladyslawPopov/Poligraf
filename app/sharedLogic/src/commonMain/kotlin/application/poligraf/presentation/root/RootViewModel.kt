package application.poligraf.presentation.root

import application.poligraf.data.base.BaseViewModel
import application.poligraf.engine.device.DeviceInfoProvider

class RootViewModel(
    private val deviceProvider: DeviceInfoProvider
) : BaseViewModel() {

    init {
        initializeApp()
    }

    private fun initializeApp() {
//        launchSafe(
//            block = {
//                // 2. Collect metadata and sync with server
//                val metadata = mapOf(
//                    "device_id" to deviceProvider.getDeviceId(),
//                    "device_model" to deviceProvider.getDeviceModel(),
//                    "os_version" to deviceProvider.getOsVersion(),
//                    "language" to deviceProvider.getLanguage(),
//                    "region" to deviceProvider.getRegion(),
//                    "timezone" to deviceProvider.getTimeZone(),
//                    "app_version" to deviceProvider.getAppVersion()
//                )
//            }
//        )
    }
}
