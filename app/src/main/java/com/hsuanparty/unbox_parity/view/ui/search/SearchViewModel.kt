package com.hsuanparty.unbox_parity.view.ui.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel;
import com.hsuanparty.unbox_parity.di.Injectable
import com.hsuanparty.unbox_parity.model.FirebaseDbManager
import com.hsuanparty.unbox_parity.model.MyPreferences
import com.hsuanparty.unbox_parity.utils.LogMessage
import com.hsuanparty.unbox_parity.utils.SimpleDelayTask
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchViewModel @Inject constructor() : ViewModel(), Injectable {

    companion object {
        private val TAG = SearchViewModel::class.java.simpleName

        const val SEARCH_START = 0
        const val SEARCH_FINISH = 1
    }

    @Inject
    lateinit var mPreferences: MyPreferences

    @Inject
    lateinit var mDbManager: FirebaseDbManager

    val isWaitingLiveData: MutableLiveData<Boolean> = MutableLiveData()

    val isSearchFinish: MutableLiveData<Int> = MutableLiveData()

    fun search(str: String) {
        if (str.isEmpty()) {
            LogMessage.D(TAG, "Search text is null or empty")
            return
        }

        LogMessage.D(TAG, "start search with string: $str")
        mPreferences.lastSearchKeyword = str

        isWaitingLiveData.value = true
        isSearchFinish.value = SEARCH_START

        mDbManager.setVideoValueEvent()
        mDbManager.setVideoChildValueEvent(item)
    }
}
