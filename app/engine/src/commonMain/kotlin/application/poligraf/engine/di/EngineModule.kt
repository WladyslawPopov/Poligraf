package application.poligraf.engine.di

import application.poligraf.engine.ads.AdsManager
import application.poligraf.engine.ads.StubAdsManager
import application.poligraf.engine.billing.BillingManager
import application.poligraf.engine.billing.StubBillingManager
import org.koin.dsl.module

val engineModule = module {
    single<AdsManager> { StubAdsManager() }
    single<BillingManager> { StubBillingManager() }
}
