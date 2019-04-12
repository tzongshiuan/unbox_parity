package com.hsuanparty.unbox_parity.view.ui.video

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.hsuanparty.unbox_parity.di.Injectable
import com.hsuanparty.unbox_parity.model.FirebaseDbManager
import com.hsuanparty.unbox_parity.model.MyPreferences
import com.hsuanparty.unbox_parity.model.VideoItem
import com.hsuanparty.unbox_parity.utils.Constants
import com.hsuanparty.unbox_parity.utils.LogMessage
import com.hsuanparty.unbox_parity.utils.youtube.YoutubeConnector
import com.hsuanparty.unbox_parity.utils.youtube.YoutubeConnector.Companion.NONE_HOT_VIDEO_COUNT
import com.hsuanparty.unbox_parity.utils.youtube.YoutubeConnector.Companion.NONE_HOT_VIDEO_UPLOAD
import com.hsuanparty.unbox_parity.utils.youtube.YoutubeConnectorV2
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoViewModel @Inject constructor() : ViewModel(), Injectable {

    companion object {
        private val TAG = VideoViewModel::class.java.simpleName

        const val ENTER_FULL_SCREEN = 0
        const val EXIT_FULL_SCREEN = 1

        const val ORDER_RELATIVE = 0
        const val ORDER_VIEW_COUNT = 1
        const val ORDER_VIEW_UPLOAD = 2
    }

    @Inject
    lateinit var mPreferences: MyPreferences

    @Inject
    lateinit var mDbManager: FirebaseDbManager

    @Inject
    lateinit var mCredential: GoogleAccountCredential

    var curOrderStatus = ORDER_RELATIVE

    val screenStatusLiveData: MutableLiveData<Int> = MutableLiveData()

    val isPerformExitFullScreen: MutableLiveData<Boolean> = MutableLiveData()

    val videoSearchResult: MutableLiveData<List<VideoItem>> = MutableLiveData()
    val videoSearchCountResult: MutableLiveData<List<VideoItem>> = MutableLiveData()
    val videoSearchUploadResult: MutableLiveData<List<VideoItem>> = MutableLiveData()

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
                val yc = YoutubeConnectorV2(mPreferences.lastSearchKeyword)

                //calling the YoutubeConnector's search method by entered keyword
                //and saving the results in list of type VideoItem class
                if (!Constants.IS_SKIP_SEARCH) {
                    when (curOrderStatus) {
                        ORDER_RELATIVE -> videoSearchResult.postValue(yc.search(YoutubeConnector.NONE_HOT_VIDEO))
                        ORDER_VIEW_COUNT -> videoSearchCountResult.postValue(yc.search(YoutubeConnector.NONE_HOT_VIDEO_COUNT))
                        ORDER_VIEW_UPLOAD -> videoSearchUploadResult.postValue(yc.search(YoutubeConnector.NONE_HOT_VIDEO_UPLOAD))
                    }
                } else {
                    val list: ArrayList<VideoItem> = ArrayList()
                    when (curOrderStatus) {
                        ORDER_RELATIVE -> videoSearchResult.postValue(list)
                        ORDER_VIEW_COUNT -> videoSearchCountResult.postValue(list)
                        ORDER_VIEW_UPLOAD -> videoSearchUploadResult.postValue(list)
                    }
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

    fun removeObservers(fragment: Fragment) {
        screenStatusLiveData.removeObservers(fragment)
        isPerformExitFullScreen.removeObservers(fragment)
        videoSearchResult.removeObservers(fragment)
        videoSearchCountResult.removeObservers(fragment)
        searchVideoFinished.removeObservers(fragment)
        curVideoItem.removeObservers(fragment)
    }
}