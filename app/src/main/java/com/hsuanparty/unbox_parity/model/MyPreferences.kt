package com.hsuanparty.unbox_parity.model

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
        private const val SETTING_RECENT_KEYWORD = "recent_keyword"
    }

    override var developer = ""
    override var recentKeywordList: ArrayList<RecentKeywordItem> = ArrayList()

    // no need to save into sharedPreferences
    override var authStatus = AuthStatus.AUTH_NONE
    override var isFinishApp = false
    override var isLogout = false
    override var lastSearchKeyword = ""
    override var curVideoItem: VideoItem? = null
    override val dayHotVideoList: ArrayList<VideoItem> = ArrayList()
    override val weekHotVideoList: ArrayList<VideoItem> = ArrayList()
    override val monthHotVideoList: ArrayList<VideoItem> = ArrayList()
    override var userName: String = ""
    override var photo: String = ""

    private fun packPreferencesToJson(): JSONObject {
        val gson = Gson()

        val jSetting = JSONObject()
        jSetting.put(SETTING_DEVELOPER, developer)
        jSetting.put(SETTING_RECENT_KEYWORD, gson.toJson(recentKeywordList))

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

            val gson = Gson()

            // read setting from preferences
            developer = jSettings.getString(SETTING_DEVELOPER)
            recentKeywordList = gson.fromJson(jSettings.getString(SETTING_RECENT_KEYWORD)
                                                , object: TypeToken<ArrayList<RecentKeywordItem>>(){}.type)
            // Trim list size
            val now = System.currentTimeMillis()
            val milliDay = 1000 * 60 * 60 * 24
            for (item in recentKeywordList) {
                if ((now - item.dateTime) > milliDay) {
                    recentKeywordList.remove(item)
                }
            }

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