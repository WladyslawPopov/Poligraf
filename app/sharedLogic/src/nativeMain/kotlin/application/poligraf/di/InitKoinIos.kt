package application.poligraf.di

import application.poligraf.engine.auth.AuthService
import application.poligraf.engine.analytics.Analytics
import application.poligraf.engine.device.DeviceIntegrity
import application.poligraf.engine.device.ReviewManager
import application.poligraf.engine.database.common.DriverFactory
import application.poligraf.uicore.theme.ResourceProvider
import application.poligraf.uicore.theme.DesignSystem
import application.poligraf.engine.config.AppConfig
import application.poligraf.engine.io.audio.AudioRecorder
import application.poligraf.engine.io.audio.IosAudioRecorder
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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
): IosAudioRecorder {
    var recorderResult: IosAudioRecorder? = null
    
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
        
        val appScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        single { appScope }
        
        val recorder = IosAudioRecorder(appScope)
        recorderResult = recorder
        
        single<IosAudioRecorder> { recorder }
        single<AudioRecorder> { recorder }
    }
    
    initKoin(
        platformModules = listOf(iosModule)
    )
    
    return recorderResult!!
}
