package com.gorilla.smart_attendance

import android.app.Activity
import android.app.Application
import android.app.Fragment
import com.hsuanparty.unbox_parity.di.AppInjector
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasFragmentInjector
import javax.inject.Inject

@Suppress("DEPRECATION")
class MyApplication : Application(), HasActivityInjector, HasFragmentInjector {

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

//        Fabric.with(this, Crashlytics())
    }

    override fun activityInjector(): AndroidInjector<Activity>? {
        return activityInjector
    }

    override fun fragmentInjector(): AndroidInjector<Fragment>? {
        return fragmentInjector
    }
}