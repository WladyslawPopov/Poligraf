package application.liedetector

import android.app.Application
import android.os.StrictMode
import application.liedetector.di.androidModule
import application.liedetector.di.initKoin
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level
import org.koin.dsl.module

class LieDetectorApp : Application() {
    
    private val appScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

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

        // 1. Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // 2. Initialize Koin
        initKoin(
            platformModules = listOf(androidModule),
            appDeclaration = {
                androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.NONE)
                androidContext(this@LieDetectorApp)
                modules(module { 
                    single { appScope }
                    single { FirebaseAuth.getInstance() }
                })
            }
        )
    }
}
