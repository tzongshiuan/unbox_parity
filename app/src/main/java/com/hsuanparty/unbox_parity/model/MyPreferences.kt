package com.hsuanparty.unbox_parity.model

import android.content.Context
import android.util.Log
import com.hsuanparty.unbox_parity.utils.DefaultSettings
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyPreferences @Inject constructor(val context: Context) : PreferencesHelper {

    companion object {
        private val TAG = MyPreferences::class.java.simpleName

        private const val APP_VERSION = "version"
        private const val MY_SETTING = "my_setting"
        private const val SETTING_DEVELOPER = "developer"
    }

    override var developer = ""

    // no need to save into sharedPreferences
    override var authStatus = AuthStatus.AUTH_NONE
    override var isFinishApp = false
    override var lastSearchKeyword = ""
    override var curVideoItem: VideoItem? = null
    override val dayHotVideoList: ArrayList<VideoItem> = ArrayList()
    override val weekHotVideoList: ArrayList<VideoItem> = ArrayList()
    override val monthHotVideoList: ArrayList<VideoItem> = ArrayList()

    private fun packPreferencesToJson(): JSONObject {
        val jSetting = JSONObject()
        jSetting.put(SETTING_DEVELOPER, developer)

        return jSetting
    }

    private fun initPreferences() {
        developer = DefaultSettings.DEVELOPER
    }

    override fun readPreferences() {
        val preferences = context.getSharedPreferences("preference", Context.MODE_PRIVATE)

        // to check whether use the setting in the same version
        val versionName = preferences.getString(APP_VERSION, "")
        if (versionName != getVersionName()) {
            Log.d(TAG, "Read reference failed, previous version ${getVersionName()}")
            initPreferences()
            return
        }

        try {
            val jSettings = JSONObject(preferences.getString(MY_SETTING, ""))
            Log.d(TAG, "Read preference Setting: $jSettings")

            // read setting from preferences
            developer = jSettings.getString(SETTING_DEVELOPER)
        } catch (e: Exception) {
            Log.d(TAG, e.toString())
        }
    }

    override fun savePreferences() {
        val preferences = context.getSharedPreferences("preference", Context.MODE_PRIVATE)
        val preferenceEditor = preferences.edit()

        preferenceEditor.putString(MY_SETTING, packPreferencesToJson().toString())
        preferenceEditor.putString(APP_VERSION, getVersionName())
        preferenceEditor.apply()
    }

    /**
     * Get App version name
     */
    private fun getVersionName(): String {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return packageInfo.versionName
    }
}