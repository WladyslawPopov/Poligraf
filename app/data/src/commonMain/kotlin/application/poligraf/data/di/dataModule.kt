package application.poligraf.data.di

import application.poligraf.data.recording.di.recordingDataModule
import org.koin.dsl.module

val dataModule = module {
    includes(
        recordingDataModule
    )
}
