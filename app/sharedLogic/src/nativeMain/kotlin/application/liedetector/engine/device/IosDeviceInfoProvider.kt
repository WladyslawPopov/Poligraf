package application.liedetector.engine.device

import platform.UIKit.UIDevice
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.languageCode
import platform.Foundation.countryCode
import platform.Foundation.NSTimeZone
import platform.Foundation.localTimeZone
import platform.Foundation.NSBundle

class IosDeviceInfoProvider : DeviceInfoProvider {
    override fun getDeviceId(): String = UIDevice.currentDevice.identifierForVendor?.UUIDString ?: "unknown_ios"
    override fun getDeviceModel(): String = UIDevice.currentDevice.model
    override fun getOsVersion(): String = "${UIDevice.currentDevice.systemName} ${UIDevice.currentDevice.systemVersion}"
    override fun getLanguage(): String = NSLocale.currentLocale.languageCode
    override fun getRegion(): String = NSLocale.currentLocale.countryCode ?: ""
    override fun getTimeZone(): String = NSTimeZone.localTimeZone.name
    override fun getAppVersion(): String {
        return NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "unknown"
    }
}
