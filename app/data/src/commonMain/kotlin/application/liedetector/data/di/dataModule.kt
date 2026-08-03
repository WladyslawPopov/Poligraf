package application.liedetector.data.di

import application.liedetector.data.user.di.userDataModule
import org.koin.dsl.module

val dataModule = module {
    includes(
        userDataModule
    )
}
