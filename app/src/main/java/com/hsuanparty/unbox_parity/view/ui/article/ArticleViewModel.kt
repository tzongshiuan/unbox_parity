package com.hsuanparty.unbox_parity.view.ui.article

import androidx.lifecycle.ViewModel;
import com.hsuanparty.unbox_parity.di.Injectable
import javax.inject.Inject

class ArticleViewModel @Inject constructor() : ViewModel(), Injectable {

    companion object {
        private val TAG = ArticleViewModel::class.java.simpleName
    }
}
