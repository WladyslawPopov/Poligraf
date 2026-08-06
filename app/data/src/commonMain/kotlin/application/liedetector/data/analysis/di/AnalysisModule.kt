package application.liedetector.data.analysis.di

import application.liedetector.data.analysis.AnalysisRepository
import application.liedetector.data.analysis.AnalysisRepositoryImpl
import application.liedetector.data.analysis.remote.AnalysisRemoteDataSource
import application.liedetector.data.analysis.remote.AnalysisRemoteDataSourceImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val analysisDataModule = module {
    singleOf(::AnalysisRemoteDataSourceImpl) bind AnalysisRemoteDataSource::class
    singleOf(::AnalysisRepositoryImpl) bind AnalysisRepository::class
}
