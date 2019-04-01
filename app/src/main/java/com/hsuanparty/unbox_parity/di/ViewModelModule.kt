package com.hsuanparty.unbox_parity.di

import androidx.lifecycle.ViewModel
import com.hsuanparty.unbox_parity.view.ui.article.ArticleViewModel
import com.hsuanparty.unbox_parity.view.ui.parity.ParityViewModel
import com.hsuanparty.unbox_parity.view.ui.search.SearchViewModel
import com.hsuanparty.unbox_parity.view.ui.setting.SettingViewModel
import com.hsuanparty.unbox_parity.view.ui.video.VideoViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Author: Tsung Hsuan, Lai
 * Created on: 2019/3/27
 */
@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(SearchViewModel::class)
    abstract fun bindSearchViewModel(searchViewModel: SearchViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(VideoViewModel::class)
    abstract fun bindVideoViewModel(videoViewModel: VideoViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ArticleViewModel::class)
    abstract fun bindArticleViewModel(articleViewModel: ArticleViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ParityViewModel::class)
    abstract fun bindParityViewModel(parityViewModel: ParityViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingViewModel::class)
    abstract fun bindSettingViewModel(settingViewModel: SettingViewModel): ViewModel
}