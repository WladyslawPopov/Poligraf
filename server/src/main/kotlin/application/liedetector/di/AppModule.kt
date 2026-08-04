package application.liedetector.di

import application.liedetector.ai.GeminiService
import application.liedetector.database.repository.*
import application.liedetector.service.*
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
