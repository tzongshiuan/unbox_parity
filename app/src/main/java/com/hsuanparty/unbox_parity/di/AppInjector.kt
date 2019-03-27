package com.hsuanparty.unbox_parity.di

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.hsuanparty.unbox_parity.MyApplication
import dagger.android.AndroidInjection
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.HasSupportFragmentInjector

/**
 * Author: Tsung Hsuan, Lai
 * Created on: 2019/3/27
 */
object AppInjector {
    fun init(application: MyApplication) {

        DaggerAppComponent.builder()
                .appModule(AppModule(application))
                .build()
                .inject(application)

        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
                    override fun onActivityCreated(activity: Activity,
                                                   savedInstanceState: Bundle?) {
                        handleActivity(activity)
                    }

                    override fun onActivityStarted(activity: Activity) {

                    }

                    override fun onActivityResumed(activity: Activity) {

                    }

                    override fun onActivityPaused(activity: Activity) {

                    }

                    override fun onActivityStopped(activity: Activity) {

                    }

                    override fun onActivitySaveInstanceState(activity: Activity,
                                                             outState: Bundle?) {

                    }

                    override fun onActivityDestroyed(activity: Activity) {

                    }
                })
    }

    private fun handleActivity(activity: Activity) {
        if (activity is Injectable || activity is HasSupportFragmentInjector) {
            AndroidInjection.inject(activity)
        }

        (activity as? FragmentActivity)?.supportFragmentManager?.registerFragmentLifecycleCallbacks(
                object : FragmentManager.FragmentLifecycleCallbacks() {
                    override fun onFragmentPreAttached(fm: FragmentManager, f: Fragment, context: Context) {
                        if (f is Injectable) {
                            AndroidSupportInjection.inject(f)
                        }
                    }
                }, true)
    }
}