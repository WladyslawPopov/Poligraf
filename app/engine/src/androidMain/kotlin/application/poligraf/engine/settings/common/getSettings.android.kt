package application.poligraf.engine.settings.common

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.mp.KoinPlatform.getKoin

actual fun getSettings(): Settings {
    val context: Context = getKoin().get()
    return SharedPreferencesSettings(
        context.getSharedPreferences("lie_detector_prefs", 0)
    )
}
