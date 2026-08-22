package application.poligraf.data.di

import application.poligraf.data.repository.UserRepositoryImpl
import application.poligraf.domain.repository.UserRepository
import org.koin.dsl.module

val dataModule = module {
    single<UserRepository> { UserRepositoryImpl(get()) }
}
