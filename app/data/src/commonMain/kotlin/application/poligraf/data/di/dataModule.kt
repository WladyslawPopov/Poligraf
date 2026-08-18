package application.poligraf.data.di

import application.poligraf.data.analysis.di.analysisDataModule
import application.poligraf.data.recording.di.recordingDataModule
import application.poligraf.data.subject.di.subjectDataModule
import application.poligraf.data.user.di.userDataModule
import org.koin.dsl.module

val dataModule = module {
    includes(
        userDataModule,
        subjectDataModule,
        analysisDataModule,
        recordingDataModule
    )
}
