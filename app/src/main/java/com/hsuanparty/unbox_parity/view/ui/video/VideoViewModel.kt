package com.hsuanparty.unbox_parity.view.ui.video

import androidx.lifecycle.ViewModel;
import com.hsuanparty.unbox_parity.di.Injectable
import javax.inject.Inject

class VideoViewModel @Inject constructor() : ViewModel(), Injectable {

    companion object {
        private val TAG = VideoViewModel::class.java.simpleName
    }
}
