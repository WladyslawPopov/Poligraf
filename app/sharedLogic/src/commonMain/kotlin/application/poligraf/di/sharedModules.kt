package application.poligraf.di

import application.poligraf.data.di.dataModule
import application.poligraf.engine.di.engineModule
import application.poligraf.ui.di.uiModule

val sharedModules = listOf(
    uiModule,
    dataModule,
    engineModule,
)
