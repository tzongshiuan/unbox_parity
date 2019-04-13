package com.hsuanparty.unbox_parity.view.ui.video

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hsuanparty.unbox_parity.R
import com.hsuanparty.unbox_parity.databinding.VideoFragmentBinding
import com.hsuanparty.unbox_parity.di.Injectable
import com.hsuanparty.unbox_parity.model.MyPreferences
import com.hsuanparty.unbox_parity.model.VideoItem
import com.hsuanparty.unbox_parity.utils.LogMessage
import com.hsuanparty.unbox_parity.utils.MyViewModelFactory
import com.hsuanparty.unbox_parity.utils.youtube.YoutubeAdapter
import com.hsuanparty.unbox_parity.view.ui.UnboxParityActivity
import com.hsuanparty.unbox_parity.view.ui.search.SearchViewModel
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener
import javax.inject.Inject

class VideoFragment : Fragment(), Injectable{

    companion object {
        private val TAG = VideoFragment::class.java.simpleName

        private const val DEFAULT_VIDEO_ID = ""

//        @JvmStatic
//        @BindingAdapter("convertVideoRelative")
//        fun convertVideoRelative(view: RecyclerView, curOrderStatus: Int) {
//            if (curOrderStatus == VideoViewModel.ORDER_RELATIVE) {
//                view.visibility = View.VISIBLE
//            } else {
//                view.visibility = View.GONE
//            }
//        }
    }

    @Inject
    lateinit var factory: MyViewModelFactory

    @Inject
    lateinit var searchViewModel: SearchViewModel

    @Inject
    lateinit var mPreferences: MyPreferences

    private lateinit var viewModel: VideoViewModel

    private lateinit var mBinding: VideoFragmentBinding

    private var player: YouTubePlayer? = null

    private var isEverSearch = false

    override fun onCreate(savedInstanceState: Bundle?) {
        LogMessage.D(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        LogMessage.D(TAG, "onCreateView()")
        mBinding = VideoFragmentBinding.inflate(inflater, container, false)
        initUI()

        return mBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        LogMessage.D(TAG, "onActivityCreated()")
        super.onActivityCreated(savedInstanceState)

        searchViewModel = ViewModelProviders.of(this, factory).get(SearchViewModel::class.java)
        searchViewModel.isSearchFinish.observe(this, Observer<Int> { status ->
            when (status) {
                SearchViewModel.SEARCH_START -> {
                    mBinding.noDataGroup.visibility = View.VISIBLE
                }

                SearchViewModel.SEARCH_FINISH -> {
                    mBinding.noDataGroup.visibility = View.GONE
                    mBinding.segmentView.visibility = View.VISIBLE
                    isEverSearch = true
                }

                else -> {}
            }
        })

        viewModel = ViewModelProviders.of(this, factory).get(VideoViewModel::class.java)
        (mBinding.recyclerView.adapter as YoutubeAdapter).videoViewModel = viewModel

        viewModel.isPerformExitFullScreen.observe(this, Observer { isExit ->
            if (isExit) {
                mBinding.youtubeView.exitFullScreen()
            }
        })

        viewModel.screenStatusLiveData.observe(this, Observer { status ->
            when (status) {
                VideoViewModel.ENTER_FULL_SCREEN -> {
                    LogMessage.D(TAG, "Player Enter FullScreen")
                    mBinding.noDataGroup.visibility = View.GONE
                }

                VideoViewModel.EXIT_FULL_SCREEN -> {
                    LogMessage.D(TAG, "Player Exit FullScreen")
                    if (!isEverSearch) {
                        mBinding.noDataGroup.visibility = View.VISIBLE
                    }
                }

                else -> {}
            }
        })

        viewModel.videoSearchResult.observe(this, Observer { result ->
            onGetSearchResult(result)
        })

        viewModel.videoSearchCountResult.observe(this, Observer { result ->
            onGetSearchResult(result)
        })

        viewModel.videoSearchUploadResult.observe(this, Observer { result ->
            onGetSearchResult(result)
        })

        viewModel.curVideoItem.observe(this, Observer { videoItem ->
            mBinding.recyclerView.adapter?.notifyDataSetChanged()
            player?.loadVideo(videoItem.id!!, 0f)

            // jump to video page forcibly
            searchViewModel.isSearchFinish.value = SearchViewModel.SWITCH_TO_VIDEO_PAGE
        })
    }

    private fun onGetSearchResult(result: List<VideoItem>) {
        (mBinding.recyclerView.adapter as YoutubeAdapter).mVideoList = result
        (mBinding.recyclerView.adapter as YoutubeAdapter).selectIndex = -1
        mBinding.recyclerView.adapter?.notifyDataSetChanged()

        viewModel.searchVideoFinished.postValue(true)

        // clear play video
        player?.cueVideo(DEFAULT_VIDEO_ID, 0f)
    }

    override fun onResume() {
        LogMessage.D(TAG, "onResume()")
        super.onResume()
    }

    override fun onPause() {
        LogMessage.D(TAG, "onPause()")
        super.onPause()
    }

    override fun onStop() {
        LogMessage.D(TAG, "onStop()")
        super.onStop()

        player?.pause()

        if (mBinding.youtubeView.isFullScreen()) {
            viewModel.performExitFullScreen()
        }
    }

    override fun onDestroy() {
        LogMessage.D(TAG, "onDestroy()")
        super.onDestroy()

        mPreferences.curVideoItem = null
        player = null
        ((mBinding.recyclerView.adapter as YoutubeAdapter).mVideoList as ArrayList<*>).clear()

        viewModel.removeObservers(this)
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//    }

    private fun initUI() {
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = RecyclerView.VERTICAL
        mBinding.recyclerView.layoutManager = layoutManager
        mBinding.recyclerView.adapter = YoutubeAdapter()

        mBinding.curOrderStatus = VideoViewModel.ORDER_RELATIVE
        mBinding.segmentView.setOnSelectionChangedListener { identifier, value ->
            LogMessage.D(TAG, "identifier: $identifier, value: $value")

            val array = resources.getStringArray(R.array.video_order_option)

            // Clear data
            val adapter = mBinding.recyclerView.adapter as YoutubeAdapter
            (adapter.mVideoList as ArrayList).clear()
            adapter.notifyDataSetChanged()

            when (value) {
                array[0] -> {
                    mBinding.curOrderStatus = VideoViewModel.ORDER_RELATIVE
                    viewModel.curOrderStatus = VideoViewModel.ORDER_RELATIVE
                }
                array[1] -> {
                    mBinding.curOrderStatus = VideoViewModel.ORDER_VIEW_COUNT
                    viewModel.curOrderStatus = VideoViewModel.ORDER_VIEW_COUNT
                }
                array[2] -> {
                    mBinding.curOrderStatus = VideoViewModel.ORDER_VIEW_UPLOAD
                    viewModel.curOrderStatus = VideoViewModel.ORDER_VIEW_UPLOAD
                }
                else -> {}
            }
            viewModel.searchVideo(activity!!)
        }

        lifecycle.addObserver(mBinding.youtubeView)
        mBinding.youtubeView.addYouTubePlayerListener(object: AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                LogMessage.D(TAG, "onReady()")

                player = youTubePlayer
                val videoId = mPreferences.curVideoItem?.id
                if (!videoId.isNullOrEmpty()) {
                    youTubePlayer.loadVideo(videoId, 0f)
                } else {
                    youTubePlayer.cueVideo(DEFAULT_VIDEO_ID, 0f)
                }
            }

            override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                LogMessage.D(TAG, "onStateChange(), state: $state")

                when (state) {
                    PlayerConstants.PlayerState.ENDED -> {
                        youTubePlayer.cueVideo(mPreferences.curVideoItem?.id!!, 0f)
                    }

                    else -> {}
                }
            }
        })

        mBinding.youtubeView.addFullScreenListener(object: YouTubePlayerFullScreenListener {
            override fun onYouTubePlayerEnterFullScreen() {
                LogMessage.D(TAG, "onYouTubePlayerEnterFullScreen()")

                viewModel.enterFullScreen()
            }

            override fun onYouTubePlayerExitFullScreen() {
                LogMessage.D(TAG, "onYouTubePlayerExitFullScreen()")

                viewModel.exitFullScreen()
            }
        })

        // TODO add youtube view
//        val btn = Button(context)
//        btn.text = "GGGGGGGGGGGGGG"
//        mBinding.youtubeView.addView(btn)
    }
}
