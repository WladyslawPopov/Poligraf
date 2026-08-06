package application.liedetector.di

import application.liedetector.engine.auth.AuthService
import application.liedetector.engine.analytics.Analytics
import application.liedetector.engine.device.DeviceIntegrity
import application.liedetector.engine.device.ReviewManager
import application.liedetector.engine.database.common.DriverFactory
import application.liedetector.uicore.theme.ResourceProvider
import application.liedetector.uicore.theme.DesignSystem
import application.liedetector.engine.config.AppConfig
import com.russhwolf.settings.Settings
import org.koin.dsl.module

/**
 * Entry point for iOS to initialize Koin with native implementations.
 */
fun doInitKoinIos(
    authService: AuthService,
    analytics: Analytics,
    integrity: DeviceIntegrity,
    reviewManager: ReviewManager,
    resourceProvider: ResourceProvider,
    driverFactory: DriverFactory,
    settings: Settings,
    appVersion: String,
    deviceId: String,
    isDebug: Boolean
) {
    val iosModule = module {
        single { authService }
        single { analytics }
        single { integrity }
        single { reviewManager }
        single { resourceProvider }
        single { DesignSystem(get(), isDebug = isDebug) }
        single { driverFactory }
        single { settings }
        single { 
            AppConfig(
                appVersion = appVersion,
                deviceId = deviceId,
                isDebug = isDebug,
                platform = "iOS"
            )
        }
    }
    
    initKoin(
        platformModules = listOf(iosModule)
    )
}
