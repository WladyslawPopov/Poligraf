package application.poligraf.data.user.di

import application.poligraf.data.user.UserRepository
import application.poligraf.data.user.UserRepositoryImpl
import application.poligraf.data.user.remote.UserRemoteDataSource
import application.poligraf.data.user.remote.UserRemoteDataSourceImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val userDataModule = module {
    singleOf(::UserRemoteDataSourceImpl) bind UserRemoteDataSource::class
    singleOf(::UserRepositoryImpl) bind UserRepository::class
}
