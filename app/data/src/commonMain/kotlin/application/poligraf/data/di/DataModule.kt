package application.poligraf.data.di

import application.poligraf.data.analyzer.AnalyzerRepositoryImpl
import application.poligraf.data.history.HistoryRepositoryImpl
import application.poligraf.data.preferences.PreferencesRepositoryImpl
import application.poligraf.domain.analyzer.repository.AnalyzerRepository
import application.poligraf.domain.history.repository.HistoryRepository
import application.poligraf.domain.preferences.repository.PreferencesRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    singleOf(::AnalyzerRepositoryImpl) bind AnalyzerRepository::class
    singleOf(::HistoryRepositoryImpl) bind HistoryRepository::class
    singleOf(::PreferencesRepositoryImpl) bind PreferencesRepository::class
}
