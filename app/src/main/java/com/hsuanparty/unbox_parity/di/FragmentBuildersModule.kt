package com.hsuanparty.unbox_parity.di

import com.hsuanparty.unbox_parity.view.ui.MainActivityFragment
import com.hsuanparty.unbox_parity.view.ui.article.ArticleFragment
import com.hsuanparty.unbox_parity.view.ui.parity.ParityFragment
import com.hsuanparty.unbox_parity.view.ui.search.SearchFragment
import com.hsuanparty.unbox_parity.view.ui.setting.SettingFragment
import com.hsuanparty.unbox_parity.view.ui.video.VideoFragment
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

    @ContributesAndroidInjector
    abstract fun contributeSearchFragment(): SearchFragment

    @ContributesAndroidInjector
    abstract fun contributeVideoFragment(): VideoFragment

    @ContributesAndroidInjector
    abstract fun contributeArticleFragment(): ArticleFragment

    @ContributesAndroidInjector
    abstract fun contributeParityFragment(): ParityFragment

    @ContributesAndroidInjector
    abstract fun contributeSettingFragment(): SettingFragment
}