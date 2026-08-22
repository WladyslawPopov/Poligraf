package application.poligraf.engine.ads

interface AdsManager {
    fun showBanner()
    fun hideBanner()
    fun showInterstitial(onDismissed: () -> Unit)
}

class StubAdsManager : AdsManager {
    override fun showBanner() {}
    override fun hideBanner() {}
    override fun showInterstitial(onDismissed: () -> Unit) { onDismissed() }
}
