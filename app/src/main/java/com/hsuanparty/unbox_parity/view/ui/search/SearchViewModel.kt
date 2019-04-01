package com.hsuanparty.unbox_parity.view.ui.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel;
import com.hsuanparty.unbox_parity.di.Injectable
import com.hsuanparty.unbox_parity.utils.LogMessage
import com.hsuanparty.unbox_parity.utils.SimpleDelayTask
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchViewModel @Inject constructor() : ViewModel(), Injectable {

    companion object {
        private val TAG = SearchViewModel::class.java.simpleName

        const val SEARCH_FINISH_STATUS = 0
    }

    val isWaitingLiveData: MutableLiveData<Boolean> = MutableLiveData()

    val isSearchFinish: MutableLiveData<Int> = MutableLiveData()

    fun search(str: String) {
        if (str.isEmpty()) {
            LogMessage.D(TAG, "Search text is null or empty")
            return
        }


        LogMessage.D(TAG, "start search with string: $str")

        isWaitingLiveData.value = true

        SimpleDelayTask.after(2000) {
            isWaitingLiveData.value = false
            isSearchFinish.value = SEARCH_FINISH_STATUS
        }
    }
}
