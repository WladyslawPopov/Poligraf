package application.poligraf.di

import application.poligraf.engine.analytics.Analytics
import application.poligraf.engine.device.DeviceIntegrity
import application.poligraf.engine.device.ReviewManager
import application.poligraf.engine.config.AppConfig
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.context.startKoin
import org.koin.dsl.module

/**
 * Entry point for iOS to initialize Koin.
 * Firebase Auth is removed, only Analytics and core services remain.
 */
fun doInitKoinIos(
    analytics: Analytics,
    integrity: DeviceIntegrity,
    reviewManager: ReviewManager,
    appVersion: String,
    deviceId: String,
    isDebug: Boolean
) {
    val iosPlatformModule = module {
        single { analytics }
        single { integrity }
        single { reviewManager }
        single {
            AppConfig(
                appVersion = appVersion,
                deviceId = deviceId,
                isDebug = isDebug,
                platform = "iOS"
            )
        }

        // Internal KMP global scope
        single { CoroutineScope(Dispatchers.Main + SupervisorJob()) }
    }
    startKoin {
        modules(iosPlatformModule)
        modules(sharedModules)
    }
}
