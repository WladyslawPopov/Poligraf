package application.liedetector.di

import application.liedetector.engine.auth.AuthService
import application.liedetector.engine.analytics.Analytics
import application.liedetector.engine.device.DeviceIntegrity
import application.liedetector.engine.device.ReviewManager
import application.liedetector.engine.database.common.DriverFactory
import application.liedetector.uicore.theme.ResourceProvider
import application.liedetector.uicore.theme.BackgroundVisualizer
import application.liedetector.uicore.theme.DesignSystem
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
    backgroundVisualizer: BackgroundVisualizer, // Keeping for flexibility, but will use factory soon
    driverFactory: DriverFactory,
    settings: Settings
) {
    val iosModule = module {
        single { authService }
        single { analytics }
        single { integrity }
        single { reviewManager }
        single { resourceProvider }
        single { backgroundVisualizer }
        single { DesignSystem(get()) }
        single { driverFactory }
        single { settings }
    }
    
    initKoin(
        platformModules = listOf(iosModule)
    )
}
