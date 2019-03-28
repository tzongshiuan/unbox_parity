package com.hsuanparty.unbox_parity.di

import com.hsuanparty.unbox_parity.view.ui.MainActivityFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Author: Tsung Hsuan, Lai
 * Created on: 2019/3/27
 */
@Module
abstract class FragmentBuildersModule {
    @ContributesAndroidInjector
    abstract fun contributeMainActivityFragment(): MainActivityFragment
}