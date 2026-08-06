package application.liedetector.data.subject.di

import application.liedetector.data.subject.SubjectRepository
import application.liedetector.data.subject.SubjectRepositoryImpl
import application.liedetector.data.subject.remote.SubjectRemoteDataSource
import application.liedetector.data.subject.remote.SubjectRemoteDataSourceImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val subjectDataModule = module {
    singleOf(::SubjectRemoteDataSourceImpl) bind SubjectRemoteDataSource::class
    singleOf(::SubjectRepositoryImpl) bind SubjectRepository::class
}
