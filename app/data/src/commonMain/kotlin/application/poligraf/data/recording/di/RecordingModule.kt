package application.poligraf.data.recording.di

import application.poligraf.data.recording.RecordingsRepository
import application.poligraf.data.recording.RecordingsRepositoryImpl
import org.koin.dsl.module

val recordingDataModule = module {
    single<RecordingsRepository> { RecordingsRepositoryImpl(get()) }
}
