package com.hsuanparty.unbox_parity.view.ui.video

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel;
import com.hsuanparty.unbox_parity.di.Injectable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoViewModel @Inject constructor() : ViewModel(), Injectable {

    companion object {
        private val TAG = VideoViewModel::class.java.simpleName

        const val ENTER_FULL_SCREEN = 0
        const val EXIT_FULL_SCREEN = 1
    }

    val screenStatusLiveData: MutableLiveData<Int> = MutableLiveData()

    val isPerformExitFullScreen: MutableLiveData<Boolean> = MutableLiveData()

    fun enterFullScreen() {
        screenStatusLiveData.value = ENTER_FULL_SCREEN
    }

    fun exitFullScreen() {
        screenStatusLiveData.value = EXIT_FULL_SCREEN
    }

    fun performExitFullScreen() {
        if (screenStatusLiveData.value == ENTER_FULL_SCREEN) {
            isPerformExitFullScreen.value = true
        }
    }
}
