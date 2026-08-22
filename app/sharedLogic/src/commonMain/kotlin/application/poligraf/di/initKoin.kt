package application.poligraf.di

import application.poligraf.data.di.dataModule
import application.poligraf.engine.database.di.databaseModule
import application.poligraf.engine.di.engineModule
import application.poligraf.engine.network.di.networkModule
import application.poligraf.engine.settings.di.settingsModule
import application.poligraf.uicore.theme.AppStringsImpl
import application.poligraf.uicore.theme.IAppStrings
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

fun initKoin(
    platformModules: List<Module> = emptyList(),
    appDeclaration: KoinAppDeclaration = {}
): KoinApplication {
    return startKoin {
        appDeclaration()
        modules(sharedModules)
        modules(platformModules)
    }
}

val uiModule = module {
    single<IAppStrings> { AppStringsImpl() }
}

val sharedModules = listOf(
    uiModule,
    networkModule,
    databaseModule,
    dataModule,
    engineModule,
    settingsModule,
)
