package application.liedetector.data.di

import application.liedetector.data.analysis.di.analysisDataModule
import application.liedetector.data.subject.di.subjectDataModule
import application.liedetector.data.user.di.userDataModule
import org.koin.dsl.module

val dataModule = module {
    includes(
        userDataModule,
        subjectDataModule,
        analysisDataModule
    )
}
