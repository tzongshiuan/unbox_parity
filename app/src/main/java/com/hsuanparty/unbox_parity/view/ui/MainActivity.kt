package com.hsuanparty.unbox_parity.view.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.facebook.CallbackManager
import com.hsuanparty.unbox_parity.R
import com.hsuanparty.unbox_parity.di.Injectable
import com.hsuanparty.unbox_parity.model.PreferencesHelper
import com.hsuanparty.unbox_parity.utils.Constants
import com.hsuanparty.unbox_parity.utils.LogMessage
import com.hsuanparty.unbox_parity.utils.SimpleDelayTask
import com.hsuanparty.unbox_parity.utils.networkChecker.NetworkChangeReceiver
import com.hsuanparty.unbox_parity.utils.networkChecker.NetworkState
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.content_main.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.inject.Inject


/**
 * Author: Tsung Hsuan, Lai
 * Created on: 2019/3/27
 */
class MainActivity : AppCompatActivity(), HasSupportFragmentInjector, Injectable {

    companion object {
        private val TAG = MainActivity::class.java.simpleName

        private const val PERMISSION_ALL = 1

        private const val MSG_NETWORK_DIALOG = 16

        class NetworkHandler(val activity: MainActivity): Handler() {

            override fun handleMessage(msg: Message) {
                LogMessage.D(TAG, "handleMessage msg.what = " + msg.what)
                when (msg.what) {
                    MSG_NETWORK_DIALOG -> {
                        activity.showNetworkCheckDialog()
                    }
                }
            }
        }
    }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var mCallbackManager: CallbackManager

    @Inject
    lateinit var mPreferences: PreferencesHelper

    private val networkReceiver: NetworkChangeReceiver by lazy { NetworkChangeReceiver() }

    private var networkDialog: AlertDialog? = null

    private val handler: Handler = NetworkHandler(this)

    private val mPermissions = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        LogMessage.D(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
        setContentView(com.hsuanparty.unbox_parity.R.layout.activity_main)

        initUI()
        initSettings()

        NetworkState.isNetworkConnected.observe(this, Observer {
            LogMessage.D(TAG, "Is network connected = ${it!!}")

            if (!it) {
                handler.sendEmptyMessageDelayed(MSG_NETWORK_DIALOG, 1500)
            } else {
                handler.removeMessages(MSG_NETWORK_DIALOG)
            }
        })

        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkReceiver, filter)

        if(!hasPermissions()){
            ActivityCompat.requestPermissions(this, mPermissions, PERMISSION_ALL)
        }

        testFireBase()
    }

    override fun onStart() {
        LogMessage.D(TAG, "onStart()")
        super.onStart()
    }

    override fun onResume() {
        LogMessage.D(TAG, "onResume()")
        super.onResume()

        mPreferences.readPreferences()
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

        mPreferences.savePreferences()
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

        val fragment = supportFragmentManager.findFragmentById(R.id.mainFragment)
        fragment?.onActivityResult(requestCode, resultCode, data)
    }

    private fun showNetworkCheckDialog() {
        if (networkDialog == null) {
            val builder = AlertDialog.Builder(this)
            builder.setCancelable(false)
            builder.setTitle(getString(R.string.txt_network_check))
            builder.setMessage(getString(R.string.msg_network_check))
            builder.setPositiveButton(getString(R.string.txt_ok)) { _, _ ->
                this.finish()
            }
            networkDialog = builder.create()
        }
        networkDialog?.show()
    }

    private fun hasPermissions(): Boolean {
        for (permission in mPermissions) {
            if (this.let { ContextCompat.checkSelfPermission(it, permission) } != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_ALL -> {
                var isAllGrant = true
                for (result in grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        isAllGrant = false
                        break
                    }
                }

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && isAllGrant) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Request permission failed, forced to close app", Toast.LENGTH_SHORT).show()
                    SimpleDelayTask.after(Constants.SPLASH_DELAY_TIME) {
                        this.finish()
                    }
                }
                return
            }
        }

        // other 'case' lines to check for other
        // permissions this app might request
    }

    private fun initSettings() {
        //getFbHash()
    }

    /**
     * keytool -exportcert -alias YOUR_RELEASE_KEY_ALIAS -keystore YOUR_RELEASE_KEY_PATH | openssl sha1 -binary | openssl base64
     */
    @SuppressLint("PackageManagerGetSignatures")
    private fun getFbHash() {
        // Add code to print out the key hash
        try {
            val info = packageManager.getPackageInfo(
                "com.hsuanparty.unbox_parity",
                PackageManager.GET_SIGNATURES
            )
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {

        } catch (e: NoSuchAlgorithmException) {

        }
    }

    override fun supportFragmentInjector() = dispatchingAndroidInjector
}
