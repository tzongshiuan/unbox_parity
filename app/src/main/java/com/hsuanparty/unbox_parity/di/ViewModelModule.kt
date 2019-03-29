package com.hsuanparty.unbox_parity.di

import androidx.lifecycle.ViewModel
import com.hsuanparty.unbox_parity.view.ui.search.SearchViewModel
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
}