package application.liedetector

import android.app.Application
import application.liedetector.di.androidModule
import application.liedetector.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class LieDetectorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        initKoin(
            platformModules = listOf(androidModule),
            appDeclaration = {
                androidLogger()
                androidContext(this@LieDetectorApp)
            }
        )
    }
}
