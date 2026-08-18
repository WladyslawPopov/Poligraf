package application.poligraf.engine.network.di

import application.poligraf.engine.network.config.NetworkConfigProvider
import application.poligraf.engine.network.config.NetworkConfigProviderImpl
import application.poligraf.engine.network.internal.getKtorClient
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val networkModule = module {
    singleOf(::NetworkConfigProviderImpl) bind NetworkConfigProvider::class
    single { getKtorClient(get(), get(), get()) }
}
