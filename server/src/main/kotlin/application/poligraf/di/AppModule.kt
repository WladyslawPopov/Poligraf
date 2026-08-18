package application.poligraf.di

import application.poligraf.ai.GeminiService
import application.poligraf.database.repository.*
import application.poligraf.service.*
import org.koin.dsl.module

val appModule = module {
    // Services
    single { GeminiService() }
    
    // Repositories
    single<UserRepository> { UserRepositoryImpl() }
    single<SubjectRepository> { SubjectRepositoryImpl() }
    single<AnalysisRepository> { AnalysisRepositoryImpl() }
    
    // Services
    single<AnalysisService> { AnalysisServiceImpl(get(), get(), get()) }
}
