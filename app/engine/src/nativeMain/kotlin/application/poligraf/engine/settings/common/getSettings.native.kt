package application.poligraf.engine.settings.common

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import platform.Foundation.NSUserDefaults

actual fun getSettings(): Settings {
    return NSUserDefaultsSettings(delegate = NSUserDefaults.standardUserDefaults())
}
