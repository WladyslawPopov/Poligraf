package application.liedetector.di

import application.liedetector.auth.AndroidAuthService
import application.liedetector.analytics.AndroidAnalytics
import application.liedetector.theme.AndroidResourceProvider
import application.liedetector.engine.auth.AuthService
import application.liedetector.engine.analytics.Analytics
import application.liedetector.engine.device.DeviceIntegrity
import application.liedetector.engine.device.ReviewManager
import application.liedetector.engine.database.common.DriverFactory
import application.liedetector.uicore.theme.ResourceProvider
import application.liedetector.BuildConfig
import application.liedetector.engine.io.audio.AndroidAudioRecorder
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import application.liedetector.uicore.theme.DesignSystem
import application.liedetector.engine.io.audio.AudioRecorder
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
