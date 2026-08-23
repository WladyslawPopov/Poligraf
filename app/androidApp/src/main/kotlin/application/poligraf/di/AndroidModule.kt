package application.poligraf.di

import application.poligraf.analytics.AndroidAnalytics
import application.poligraf.engine.analytics.Analytics
import application.poligraf.engine.device.DeviceIntegrity
import application.poligraf.engine.device.ReviewManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidModule = module {

    single<Analytics> { AndroidAnalytics(androidContext()) }

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
}
