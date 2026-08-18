package application.poligraf.engine.device

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import application.poligraf.engine.device.DeviceInfoProvider
import java.util.*

class AndroidDeviceInfoProvider(private val context: Context) : DeviceInfoProvider {

    @SuppressLint("HardwareIds")
    override fun getDeviceId(): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown_android"
    }

    override fun getDeviceModel(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer)) {
            model.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        } else {
            "${manufacturer.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }} $model"
        }
    }

    override fun getOsVersion(): String = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"

    override fun getLanguage(): String = Locale.getDefault().language

    override fun getRegion(): String = Locale.getDefault().country

    override fun getTimeZone(): String = TimeZone.getDefault().id

    override fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }
}
