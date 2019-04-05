package com.hsuanparty.unbox_parity.view.ui.video

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.hsuanparty.unbox_parity.di.Injectable
import com.hsuanparty.unbox_parity.model.FirebaseDbManager
import com.hsuanparty.unbox_parity.model.MyPreferences
import com.hsuanparty.unbox_parity.utils.Constants
import com.hsuanparty.unbox_parity.utils.LogMessage
import com.hsuanparty.unbox_parity.model.VideoItem
import com.hsuanparty.unbox_parity.utils.youtube.YoutubeConnector
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoViewModel @Inject constructor() : ViewModel(), Injectable {

    companion object {
        private val TAG = VideoViewModel::class.java.simpleName

        const val ENTER_FULL_SCREEN = 0
        const val EXIT_FULL_SCREEN = 1
    }

    @Inject
    lateinit var mPreferences: MyPreferences

    @Inject
    lateinit var mDbManager: FirebaseDbManager

    @Inject
    lateinit var mCredential: GoogleAccountCredential

    val screenStatusLiveData: MutableLiveData<Int> = MutableLiveData()

    val isPerformExitFullScreen: MutableLiveData<Boolean> = MutableLiveData()

    val videoSearchResult: MutableLiveData<List<VideoItem>> = MutableLiveData()

    val searchVideoFinished: MutableLiveData<Boolean> = MutableLiveData()

    val curVideoItem: MutableLiveData<VideoItem> = MutableLiveData()

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

    fun searchVideo(activity: Activity) {
        searchVideoFinished.value = false

        object : Thread() {
            //implementing run method
            override fun run() {
                //create our YoutubeConnector class's object with Activity context as argument
                val yc = YoutubeConnector(mCredential)

                //calling the YoutubeConnector's search method by entered keyword
                //and saving the results in list of type VideoItem class
                if (!Constants.IS_SKIP_SEARCH) {
                    videoSearchResult.postValue(yc.search(mPreferences.lastSearchKeyword + " 開箱"))
                } else {
                    val list: ArrayList<VideoItem> = ArrayList()
                    videoSearchResult.postValue(list)
                }
            }
            //starting the thread
        }.start()
    }

    fun playVideo(item: VideoItem?) {
        LogMessage.D(TAG, "play video, id: ${item?.id}, title: ${item?.title}")

        mPreferences.curVideoItem = item
        curVideoItem.value = item
    }
}