package application.poligraf.di

import application.poligraf.BuildConfig
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import application.poligraf.analytics.AndroidAnalytics
import application.poligraf.auth.AndroidAuthService
import application.poligraf.engine.analytics.Analytics
import application.poligraf.engine.auth.AuthService
import application.poligraf.engine.database.common.DriverFactory
import application.poligraf.engine.device.DeviceIntegrity
import application.poligraf.engine.device.ReviewManager
import application.poligraf.engine.io.audio.AndroidAudioRecorder
import application.poligraf.engine.io.audio.AudioRecorder
import application.poligraf.theme.AndroidResourceProvider
import application.poligraf.uicore.theme.DesignSystem
import application.poligraf.uicore.theme.ResourceProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidModule = module {
    // 1. Native Authentication
    single<AuthService> { AndroidAuthService() }
    
    // 2. Native Analytics
    single<Analytics> { AndroidAnalytics(androidContext()) }
    
    // 3. Design System
    single<ResourceProvider> { AndroidResourceProvider(androidContext()) }
    single { DesignSystem(get(), isDebug = BuildConfig.DEBUG) }
    
    // 4. Native Database Driver Factory
    single { DriverFactory(androidContext()) }
    
    // 5. Native Settings (SharedPreferences)
    single<Settings> { 
        SharedPreferencesSettings(
            androidContext().getSharedPreferences("lie_detector_prefs", 0)
        ) 
    }
    
    // 6. Native Device Services (Placeholders)
    single<DeviceIntegrity> { 
        object : DeviceIntegrity {
            override suspend fun checkIntegrity(): Boolean = true 
        } 
    }
    single<ReviewManager> {
        object : ReviewManager { 
            override suspend fun requestReview(): Boolean = true 
        } 
    }

    // 7. Audio Recorder
    single<AudioRecorder> { AndroidAudioRecorder(androidContext(), get()) }
}
