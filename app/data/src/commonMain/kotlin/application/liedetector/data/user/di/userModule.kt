package application.liedetector.data.user.di

import application.liedetector.data.user.UserRepository
import application.liedetector.data.user.UserRepositoryImpl
import application.liedetector.data.user.remote.UserRemoteDataSource
import application.liedetector.data.user.remote.UserRemoteDataSourceImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val userDataModule = module {
    singleOf(::UserRemoteDataSourceImpl) bind UserRemoteDataSource::class
    singleOf(::UserRepositoryImpl) bind UserRepository::class
}
