package com.hsuanparty.unbox_parity.view.ui.search

import androidx.lifecycle.ViewModel;
import com.hsuanparty.unbox_parity.di.Injectable
import javax.inject.Inject

class SearchViewModel @Inject constructor() : ViewModel(), Injectable {

    companion object {
        private val TAG = SearchViewModel::class.java.simpleName
    }
}
