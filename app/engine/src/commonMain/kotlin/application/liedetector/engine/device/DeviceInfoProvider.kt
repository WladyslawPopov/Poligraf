package application.liedetector.engine.device

interface DeviceInfoProvider {
    fun getDeviceId(): String
    fun getDeviceModel(): String
    fun getOsVersion(): String
    fun getLanguage(): String
    fun getRegion(): String
    fun getTimeZone(): String
    fun getAppVersion(): String
}
