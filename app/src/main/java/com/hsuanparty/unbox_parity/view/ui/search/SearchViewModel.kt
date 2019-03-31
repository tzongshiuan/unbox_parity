package com.hsuanparty.unbox_parity.view.ui.search

import androidx.lifecycle.ViewModel;
import com.hsuanparty.unbox_parity.di.Injectable
import com.hsuanparty.unbox_parity.utils.LogMessage
import javax.inject.Inject

class SearchViewModel @Inject constructor() : ViewModel(), Injectable {

    companion object {
        private val TAG = SearchViewModel::class.java.simpleName
    }

    fun search(str: String) {
        LogMessage.D(TAG, "start search with string: $str")
    }
}
