package application.liedetector.data.recording.di

import application.liedetector.data.recording.RecordingsRepository
import application.liedetector.data.recording.RecordingsRepositoryImpl
import org.koin.dsl.module

val recordingDataModule = module {
    single<RecordingsRepository> { RecordingsRepositoryImpl(get()) }
}
