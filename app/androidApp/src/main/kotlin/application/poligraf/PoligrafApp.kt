package application.poligraf

import android.annotation.SuppressLint
import android.app.Application
import android.os.StrictMode
import android.provider.Settings
import application.poligraf.di.androidModule
import application.poligraf.di.sharedModules
import application.poligraf.engine.config.AppConfig
import com.google.firebase.FirebaseApp
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module

class PoligrafApp : Application() {
    
    private val appScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    @SuppressLint("HardwareIds")
    override fun onCreate() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .build()
            )
        }
        
        super.onCreate()

        // 0. Initialize Logging
        if (BuildConfig.DEBUG) {
            Napier.base(DebugAntilog())
        }

        // 1. Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // 2. Initialize Koin
        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.NONE)
            androidContext(this@PoligrafApp)
            modules(module {
                single { appScope }
                single {
                    AppConfig(
                        appVersion = BuildConfig.VERSION_NAME,
                        deviceId = Settings.Secure.getString(
                            contentResolver,
                            Settings.Secure.ANDROID_ID
                        ) ?: "unknown_android",
                        isDebug = BuildConfig.DEBUG,
                        platform = "Android"
                    )
                }
            })
            modules(androidModule)
            modules(sharedModules)
        }
    }
}
