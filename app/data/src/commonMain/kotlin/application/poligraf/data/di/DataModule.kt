package application.poligraf.data.di

import application.poligraf.data.repository.AnalyzerRepositoryImpl
import application.poligraf.domain.repository.AnalyzerRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    singleOf(::AnalyzerRepositoryImpl) bind AnalyzerRepository::class
}
