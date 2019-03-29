package com.hsuanparty.unbox_parity.di

import com.hsuanparty.unbox_parity.view.ui.MainActivity
import com.hsuanparty.unbox_parity.view.ui.UnboxParityActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Author: Tsung Hsuan, Lai
 * Created on: 2019/3/27
 */
@Module
abstract class ActivityBuildersModule {

    @ContributesAndroidInjector(modules = [FragmentBuildersModule::class])
    abstract fun contributeMainActivity(): MainActivity

    @ContributesAndroidInjector(modules = [FragmentBuildersModule::class])
    abstract fun contributeUnboxParityActivity(): UnboxParityActivity
}