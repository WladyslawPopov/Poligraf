package application.poligraf.ui.di

import application.poligraf.ui.theme.AppStringsImpl
import application.poligraf.ui.theme.IAppStrings
import org.koin.dsl.module

val uiModule = module {
    single<IAppStrings> { AppStringsImpl() }
}
