package com.hsuanparty.unbox_parity.utils

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.widget.Button
import android.widget.TextView
import com.hsuanparty.unbox_parity.R


/**
 * Author: Tsung Hsuan, Lai
 * Created on: 2019/4/15
 * Description:
 */
class AppRater {
    companion object {
        private val TAG = AppRater::class.java.simpleName
    }

    private lateinit var appTitle: String // App Name
    private val APP_PNAME = "com.hsuanparty.unbox_parity"// Package Name
    private val DAYS_UNTIL_PROMPT = 3 * 24 * 60 * 60 * 1000   //Min number of days
    private val LAUNCHES_UNTIL_PROMPT = 3   //Min number of launches

    fun appLaunched(activity: Activity) {
        appTitle = String.format(activity.getString(R.string.txt_rate), activity.getString(R.string.appName))

        val prefs = activity.getSharedPreferences("apprater", 0)
        if (prefs.getBoolean("dontshowagain", false)) {
            return
        }

        val editor = prefs.edit()

        // Increment launch counter
        val launchCount = prefs.getLong("launchCount", 0) + 1
        editor.putLong("launch_count", launchCount)

        // Get date of first launch
        var dateFirstLaunch: Long? = prefs.getLong("dateFirstLaunch", 0)
        LogMessage.D(TAG, "dateFirstLaunch: $dateFirstLaunch")
        if (dateFirstLaunch == 0L) {
            dateFirstLaunch = System.currentTimeMillis()
            editor.putLong("dateFirstLaunch", dateFirstLaunch)
        }
        // Wait at least n days before opening
        if (launchCount >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= dateFirstLaunch!! + DAYS_UNTIL_PROMPT) {
                showRateDialog(activity, editor)
            }
        }

        editor.apply()
    }

    private fun showRateDialog(activity: Activity, editor: SharedPreferences.Editor?) {
        LogMessage.D(TAG, "showRateDialog()")

        val dialog = Dialog(activity)
        dialog.setContentView(R.layout.dialog_rate)

        val titleView = dialog.findViewById<TextView>(R.id.rateTxt)
        titleView.text = appTitle

        val b1 = dialog.findViewById<Button>(R.id.rateBtn)
        b1.text = activity.getString(R.string.txt_give_comment)
        b1.setOnClickListener {
            activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$APP_PNAME")))
            dialog.dismiss()
        }

        val b2 = dialog.findViewById<Button>(R.id.remindBtn)
        b2.text = activity.getString(R.string.txt_remind_later)
        b2.setOnClickListener {
            dialog.dismiss()
        }

        val b3 = dialog.findViewById<Button>(R.id.skipBtn)
        b3.text = activity.getString(R.string.txt_no_thanks)
        b3.setOnClickListener{
            if (editor != null) {
                editor.putBoolean("dontshowagain", true)
                editor.apply()
            }
            dialog.dismiss()
        }

        dialog.show()
    }
}