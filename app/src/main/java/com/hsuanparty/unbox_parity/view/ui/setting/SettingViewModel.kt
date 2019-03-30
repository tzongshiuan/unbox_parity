package com.hsuanparty.unbox_parity.view.ui.setting

import androidx.lifecycle.ViewModel;
import com.hsuanparty.unbox_parity.di.Injectable
import javax.inject.Inject

class SettingViewModel @Inject constructor() : ViewModel(), Injectable {

    companion object {
        private val TAG = SettingViewModel::class.java.simpleName
    }
}
