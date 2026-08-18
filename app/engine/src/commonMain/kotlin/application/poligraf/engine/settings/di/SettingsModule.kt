package application.poligraf.engine.settings.di

import application.poligraf.engine.settings.SettingsRepository
import application.poligraf.engine.settings.SettingsRepositoryImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val settingsModule = module {
    // Settings объект будет предоставлен платформой в стартовом графе
    singleOf(::SettingsRepositoryImpl) bind SettingsRepository::class
}
