package com.hsuanparty.unbox_parity.view.ui.scan

import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel;
import com.hsuanparty.unbox_parity.di.Injectable
import com.hsuanparty.unbox_parity.model.FirebaseDbManager
import com.hsuanparty.unbox_parity.model.MyPreferences
import com.hsuanparty.unbox_parity.model.RecentKeywordItem
import com.hsuanparty.unbox_parity.utils.LogMessage
import com.hsuanparty.unbox_parity.utils.SimpleDelayTask
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanViewModel @Inject constructor() : ViewModel(), Injectable {

    companion object {
        private val TAG = ScanViewModel::class.java.simpleName
    }

    @Inject
    lateinit var mPreferences: MyPreferences

    val isEnableOCR: MutableLiveData<Boolean> = MutableLiveData()
}
