package application.liedetector.di

import com.russhwolf.settings.Settings
import com.russhwolf.settings.NSUserDefaultsSettings
import platform.Foundation.NSUserDefaults
import application.liedetector.theme.IosBackgroundVisualizer
import application.liedetector.uicore.theme.BackgroundVisualizer

/**
 * Factory for iOS settings to avoid scope issues in Swift.
 */
fun createIosSettings(): Settings {
    return NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)
}

/**
 * Factory for iOS background visualizer to avoid scope issues in Swift.
 */
fun createIosBackgroundVisualizer(): BackgroundVisualizer {
    return IosBackgroundVisualizer()
}
