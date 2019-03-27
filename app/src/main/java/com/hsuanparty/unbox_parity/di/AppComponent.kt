package com.hsuanparty.unbox_parity.di

import android.app.Application
import com.hsuanparty.unbox_parity.MyApplication
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

/**
 * Author: Tsung Hsuan, Lai
 * Created on: 2019/3/27
 */
@Singleton
@Component(modules = [AndroidSupportInjectionModule::class, AppModule::class, ActivityBuildersModule::class])
interface AppComponent {
    fun application(): Application
    fun inject(app: MyApplication)
}