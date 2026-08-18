package application.poligraf.domain.usecase.recording.di

import application.liedetector.domain.usecase.recording.DeleteRecordingUseCase
import application.liedetector.domain.usecase.recording.GetRecordingsUseCase
import application.liedetector.domain.usecase.recording.LoadRecordingsUseCase
import application.liedetector.domain.usecase.recording.SaveRecordingUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val recordingUseCaseModule = module {
    factoryOf(::GetRecordingsUseCase)
    factoryOf(::SaveRecordingUseCase)
    factoryOf(::DeleteRecordingUseCase)
    factoryOf(::LoadRecordingsUseCase)
}
