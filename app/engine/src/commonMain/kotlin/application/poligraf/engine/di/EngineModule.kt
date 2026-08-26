package application.poligraf.engine.di

import application.poligraf.database.PoligrafDatabase
import application.poligraf.engine.database.CacheRepository
import application.poligraf.engine.database.common.createDriver
import application.poligraf.engine.database.internal.CacheRepositoryImpl
import application.poligraf.engine.device.PermissionManager
import application.poligraf.engine.device.common.getPermissionManager
import application.poligraf.engine.io.audio.AudioRecorder
import application.poligraf.engine.io.audio.common.getAudioRecorder
import application.poligraf.engine.network.internal.getKtorClient
import application.poligraf.engine.settings.SettingsRepository
import application.poligraf.engine.settings.SettingsRepositoryImpl
import application.poligraf.engine.settings.common.getSettings
import application.poligraf.engine.theme.ThemeManager
import application.poligraf.engine.theme.ThemeManagerImpl
import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val engineModule = module {
    // Theme
    single<ThemeManager> { ThemeManagerImpl() }

    // Database
    single {
        val driver = createDriver()
        PoligrafDatabase(driver)
    }
    single<CacheRepository> { CacheRepositoryImpl(get()) }

    // Settings
    single<Settings> { getSettings() }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }

    // Permission Manager
    singleOf(::getPermissionManager) bind PermissionManager::class

    //Audio Recorder
    singleOf(::getAudioRecorder) bind AudioRecorder::class

    //Ktor
    singleOf(::getKtorClient) bind HttpClient::class
}
