package application.poligraf.data.analysis.di

import application.poligraf.data.analysis.AnalysisRepository
import application.poligraf.data.analysis.AnalysisRepositoryImpl
import application.poligraf.data.analysis.remote.AnalysisRemoteDataSource
import application.poligraf.data.analysis.remote.AnalysisRemoteDataSourceImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val analysisDataModule = module {
    singleOf(::AnalysisRemoteDataSourceImpl) bind AnalysisRemoteDataSource::class
    singleOf(::AnalysisRepositoryImpl) bind AnalysisRepository::class
}
