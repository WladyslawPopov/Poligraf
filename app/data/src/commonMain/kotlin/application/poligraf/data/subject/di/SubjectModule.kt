package application.poligraf.data.subject.di

import application.poligraf.data.subject.SubjectRepository
import application.poligraf.data.subject.SubjectRepositoryImpl
import application.poligraf.data.subject.remote.SubjectRemoteDataSource
import application.poligraf.data.subject.remote.SubjectRemoteDataSourceImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val subjectDataModule = module {
    singleOf(::SubjectRemoteDataSourceImpl) bind SubjectRemoteDataSource::class
    singleOf(::SubjectRepositoryImpl) bind SubjectRepository::class
}
