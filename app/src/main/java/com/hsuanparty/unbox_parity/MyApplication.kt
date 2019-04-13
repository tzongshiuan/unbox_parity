package com.hsuanparty.unbox_parity

import android.app.Activity
import android.app.Application
import androidx.fragment.app.Fragment
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.iid.FirebaseInstanceId
import com.hsuanparty.unbox_parity.di.AppInjector
import com.hsuanparty.unbox_parity.utils.LogMessage
import com.hsuanparty.unbox_parity.utils.MyFirebaseMessagingService
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

@Suppress("DEPRECATION")
class MyApplication : Application(), HasActivityInjector, HasSupportFragmentInjector {

    @Inject
    lateinit var activityInjector: DispatchingAndroidInjector<Activity>

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    override fun onCreate() {
        super.onCreate()
//        if (BuildConfig.DEBUG) {
//            Timber.plant(Timber.DebugTree())
//            Stetho.initializeWithDefaults(this)
//        }

        // Dagger2
        AppInjector.init(this)

        // Ads
        MobileAds.initialize(this, getString(R.string.admob_app_id))

        FirebaseApp.initializeApp(this)
        FacebookSdk.sdkInitialize(this)
        AppEventsLogger.activateApp(this)

        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result?.token
                LogMessage.D(MyFirebaseMessagingService::class.java.simpleName, "token: $token")
            }
        }

//        Fabric.with(this, Crashlytics())
    }

    override fun activityInjector(): AndroidInjector<Activity>? {
        return activityInjector
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment>? {
        return fragmentInjector
    }
}