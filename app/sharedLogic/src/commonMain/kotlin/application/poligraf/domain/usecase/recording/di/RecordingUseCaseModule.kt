package application.poligraf.domain.usecase.recording.di

import application.poligraf.domain.usecase.recording.DeleteRecordingUseCase
import application.poligraf.domain.usecase.recording.GetRecordingsUseCase
import application.poligraf.domain.usecase.recording.LoadRecordingsUseCase
import application.poligraf.domain.usecase.recording.SaveRecordingUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val recordingUseCaseModule = module {
    factoryOf(::GetRecordingsUseCase)
    factoryOf(::SaveRecordingUseCase)
    factoryOf(::DeleteRecordingUseCase)
    factoryOf(::LoadRecordingsUseCase)
}
