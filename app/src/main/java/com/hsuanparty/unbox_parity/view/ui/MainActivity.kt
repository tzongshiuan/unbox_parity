package com.hsuanparty.unbox_parity.view.ui

import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseApp
import com.hsuanparty.unbox_parity.utils.LogMessage
import com.hsuanparty.unbox_parity.utils.networkChecker.NetworkChangeReceiver
import com.hsuanparty.unbox_parity.utils.networkChecker.NetworkState
import com.hsuanparty.unbox_parity.R
import kotlinx.android.synthetic.main.activity_main.*



/**
 * Author: Tsung Hsuan, Lai
 * Created on: 2019/3/27
 */
class MainActivity : AppCompatActivity() {

    private val networkReceiver: NetworkChangeReceiver by lazy { NetworkChangeReceiver() }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        LogMessage.D(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
        setContentView(com.hsuanparty.unbox_parity.R.layout.activity_main)

        initUI()

        NetworkState.isNetworkConnected.observe(this, Observer {
            LogMessage.D(TAG, "Is network connected = ${it!!}")
        })

        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkReceiver, filter)

        testFireBase()
    }

    override fun onStart() {
        LogMessage.D(TAG, "onStart()")
        super.onStart()
    }

    override fun onResume() {
        LogMessage.D(TAG, "onResume()")
        super.onResume()
    }

    override fun onNewIntent(intent: Intent?) {
        LogMessage.D(TAG, "onNewIntent(), action = ${intent?.action}")
        super.onNewIntent(intent)
    }

    override fun onPause() {
        LogMessage.D(TAG, "onPause()")
        super.onPause()
    }

    override fun onStop() {
        LogMessage.D(TAG, "onStop()")
        super.onStop()
    }

    override fun onDestroy() {
        LogMessage.D(TAG, "onDestroy()")
        super.onDestroy()

        unregisterReceiver(networkReceiver)
    }

    override fun onUserInteraction() {
        LogMessage.D(TAG, "onUserInteraction()")
        super.onUserInteraction()
    }

    override fun onBackPressed() {
        LogMessage.D(TAG, "onBackPressed()")
        super.onBackPressed()
    }

    private fun initUI() {
        LogMessage.D(TAG, "initUI()")
    }

    private fun testFireBase() {
        // test firebase log event
//        val bundle = Bundle()
//        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "1212")
//        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "hsuan")
//        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image")
//        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)

        // test crashlytics
//        Crashlytics.getInstance().crash()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        LogMessage.D(TAG, "onActivityResult()")
        //callbackManager.onActivityResult(requestCode, resultCode, data)
    }
}
