package application.poligraf.engine.device

import application.poligraf.engine.device.DeviceInfoProvider
import platform.Foundation.NSBundle
import platform.Foundation.NSLocale
import platform.Foundation.NSTimeZone
import platform.Foundation.countryCode
import platform.Foundation.currentLocale
import platform.Foundation.defaultTimeZone
import platform.Foundation.languageCode
import platform.UIKit.UIDevice

class IosDeviceInfoProvider : DeviceInfoProvider {

    override fun getDeviceId(): String {
        return UIDevice.currentDevice.identifierForVendor?.UUIDString ?: "unknown_ios"
    }

    override fun getDeviceModel(): String {
        return UIDevice.currentDevice.model
    }

    override fun getOsVersion(): String {
        return "${UIDevice.currentDevice.systemName} ${UIDevice.currentDevice.systemVersion}"
    }

    override fun getLanguage(): String {
        return NSLocale.currentLocale.languageCode
    }

    override fun getRegion(): String {
        return NSLocale.currentLocale.countryCode ?: ""
    }

    override fun getTimeZone(): String {
        return NSTimeZone.defaultTimeZone.name
    }

    override fun getAppVersion(): String {
        return NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "unknown"
    }
}
