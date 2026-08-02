package application.liedetector.engine.device

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import java.util.*

class AndroidDeviceInfoProvider(private val context: Context) : DeviceInfoProvider {
    @SuppressLint("HardwareIds")
    override fun getDeviceId(): String = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown_android"
    override fun getDeviceModel(): String = "${Build.MANUFACTURER} ${Build.MODEL}"
    override fun getOsVersion(): String = "Android ${Build.VERSION.RELEASE}"
    override fun getLanguage(): String = Locale.getDefault().language
    override fun getRegion(): String = Locale.getDefault().country
    override fun getTimeZone(): String = TimeZone.getDefault().id
    override fun getAppVersion(): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }
}
