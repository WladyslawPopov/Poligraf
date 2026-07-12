package application.liedetector.di

import application.liedetector.auth.AndroidAuthService
import application.liedetector.engine.auth.AuthService
import application.liedetector.engine.database.common.DriverFactory
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidModule = module {
    single<AuthService> { AndroidAuthService() }
    single { DriverFactory(androidContext()) }
    single<Settings> { SharedPreferencesSettings(androidContext().getSharedPreferences("app_settings", 0)) }
}
