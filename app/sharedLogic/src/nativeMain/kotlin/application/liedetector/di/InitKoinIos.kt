package application.liedetector.di

import application.liedetector.engine.auth.AuthService
import application.liedetector.engine.analytics.Analytics
import application.liedetector.engine.device.DeviceIntegrity
import application.liedetector.engine.device.ReviewManager
import application.liedetector.engine.database.common.DriverFactory
import application.liedetector.uicore.theme.ResourceProvider
import application.liedetector.uicore.theme.DesignSystem
import application.liedetector.engine.config.AppConfig
import application.liedetector.engine.io.FileSystem
import application.liedetector.engine.io.IosFileSystem
import application.liedetector.engine.io.audio.AudioRecorder
import application.liedetector.engine.io.audio.IosAudioRecorder
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
        single<FileSystem> { IosFileSystem() }
    }
    
    initKoin(
        platformModules = listOf(iosModule)
    )
    
    return recorderResult!!
}
