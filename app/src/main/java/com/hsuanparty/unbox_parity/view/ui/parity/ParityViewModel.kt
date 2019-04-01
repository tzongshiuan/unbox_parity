package com.hsuanparty.unbox_parity.view.ui.parity

import androidx.lifecycle.ViewModel;
import com.hsuanparty.unbox_parity.di.Injectable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ParityViewModel @Inject constructor() : ViewModel(), Injectable {

    companion object {
        private val TAG = ParityViewModel::class.java.simpleName
    }
}
